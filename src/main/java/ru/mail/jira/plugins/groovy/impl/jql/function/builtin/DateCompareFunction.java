package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.DurationUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.*;
import ru.mail.jira.plugins.groovy.util.AntlrUtil;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.FieldUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DateCompareFunction extends AbstractBuiltInQueryFunction {
    private static final Map<String, DateUtils.Duration> TOKEN_MAP = ImmutableMap.of(
        "w", DateUtils.Duration.WEEK,
        "d", DateUtils.Duration.DAY,
        "h", DateUtils.Duration.HOUR,
        "m", DateUtils.Duration.MINUTE
    );

    private final Logger logger = LoggerFactory.getLogger(DateCompareFunction.class);

    private final FieldManager fieldManager;
    private final LocaleManager localeManager;
    private final SearchService searchService;
    private final SearchProvider searchProvider;
    private final TimeZoneManager timeZoneManager;
    private final CustomFieldManager customFieldManager;

    @Autowired
    public DateCompareFunction(
        @ComponentImport FieldManager fieldManager,
        @ComponentImport LocaleManager localeManager,
        @ComponentImport SearchService searchService,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport CustomFieldManager customFieldManager
    ) {
        super("dateCompare", 2);
        this.fieldManager = fieldManager;
        this.localeManager = localeManager;
        this.searchService = searchService;
        this.searchProvider = searchProvider;
        this.timeZoneManager = timeZoneManager;
        this.customFieldManager = customFieldManager;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        List<String> args = operand.getArgs();

        String queryString = args.get(0);
        String compareQueryString = args.get(1);

        SearchService.ParseResult parseResult = searchService.parseQuery(user, queryString);

        if (!parseResult.isValid()) {
            messageSet.addMessageSet(parseResult.getErrors());
        }

        parseQuery(messageSet, user, compareQueryString);
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        MessageSet messageSet = new MessageSetImpl();

        String queryString = args.get(0);
        String compareQueryString = args.get(1);

        SearchService.ParseResult parseResult = searchService.parseQuery(user, queryString);

        if (!parseResult.isValid()) {
            logger.error("invalid query \"{}\": ", queryString, parseResult.getErrors());
            return QueryFactoryResult.createFalseResult();
        }

        Query query = parseResult.getQuery();

        DateCompareQuery dateCompareQuery = parseQuery(messageSet, user, compareQueryString);

        if (messageSet.hasAnyErrors()) {
            logger.error("Has errors {}", messageSet.getErrorMessages());
            return QueryFactoryResult.createFalseResult();
        }

        DateCompareCollector collector = new DateCompareCollector(
            dateCompareQuery,
            timeZoneManager.getTimeZoneforUser(user).toZoneId()
        );

        BooleanQuery luceneQuery = new BooleanQuery();
        luceneQuery.add(
            new TermQuery(new Term(
                DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, dateCompareQuery.getLeftField()
            )),
            BooleanClause.Occur.MUST
        );
        luceneQuery.add(
            new TermQuery(new Term(
                DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, dateCompareQuery.getRightField()
            )),
            BooleanClause.Occur.MUST
        );

        try {
            searchProvider.search(query, user, collector, luceneQuery);
        } catch (SearchException e) {
            logger.error("caught exception while searching", e);
        }

        return new QueryFactoryResult(
            new ConstantScoreQuery(new IssueIdFilter(collector.issueIds)),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    private DateCompareQuery parseQuery(MessageSet messageSet, ApplicationUser user, String queryString) {
        DateComparisonQueryLexer lexer = new DateComparisonQueryLexer(CharStreams.fromString(queryString));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        DateComparisonQueryParser parser = new DateComparisonQueryParser(tokenStream);

        DateComparisonQueryListener listener = new DateComparisonQueryListener(localeManager.getLocaleFor(user));

        parser.addParseListener(listener);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                messageSet.addErrorMessage("Parsing error at " + charPositionInLine + ": " + msg);
            }
        });

        parser.date_comparison_query();

        return listener.buildQuery();
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    private enum DateCompareOperation {
        GT, LT, GTE, LTE, EQ;

        static DateCompareOperation fromString(String string) {
            switch (string) {
                case ">":
                    return GT;
                case "<":
                    return LT;
                case ">=":
                    return GTE;
                case "<=":
                    return LTE;
                case "=":
                    return EQ;
            }

            return null;
        }
    }

    @Getter @AllArgsConstructor
    private static class DateCompareQuery {
        private final DateCompareOperation operation;
        private final String leftField;
        private final long leftModifier;
        private final boolean leftDate;
        private final String rightField;
        private final long rightModifier;
        private final boolean rightDate;
    }

    private static class DateCompareCollector extends Collector {
        private final Set<String> issueIds = new HashSet<>();
        private final DateCompareQuery query;
        private final FieldSelector fieldSelector;
        private final ZoneId userTz;

        private IndexReader indexReader;

        private DateCompareCollector(DateCompareQuery query, ZoneId userTz) {
            this.query = query;
            this.userTz = userTz;

            fieldSelector = new SetBasedFieldSelector(ImmutableSet.of(
                query.getLeftField(),
                query.getRightField(),
                DocumentConstants.ISSUE_ID
            ), ImmutableSet.of());
        }

        @Override
        public void setScorer(Scorer scorer) {

        }

        @Override
        public void collect(int i) throws IOException {
            Document document = indexReader.document(i, fieldSelector);

            String rawLeftValue = document.get(query.getLeftField());
            String rawRightValue = document.get(query.getRightField());

            if (rawLeftValue == null || rawRightValue == null) {
                return;
            }

            Instant leftInstant = (query.isLeftDate() ? parseLocalDate(rawLeftValue) : parseDateTime(rawLeftValue)).plusSeconds(query.leftModifier);
            Instant rightInstant = (query.isRightDate() ? parseLocalDate(rawRightValue) : parseDateTime(rawRightValue)).plusSeconds(query.rightModifier);

            if (compare(leftInstant.compareTo(rightInstant))) {
                issueIds.add(document.get(DocumentConstants.ISSUE_ID));
            }
        }

        @Override
        public void setNextReader(IndexReader indexReader, int i) {
            this.indexReader = indexReader;
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

        private Instant parseLocalDate(String rawValue) {
            LocalDate localDate = LuceneUtils.stringToLocalDate(rawValue);

            return java.time.LocalDate
                .of(localDate.getYear(), localDate.getMonth(), localDate.getDay())
                .atTime(LocalTime.MIN)
                .atZone(userTz)
                .toInstant();
        }

        private boolean compare(int comparisonResult) {
            switch (query.getOperation()) {
                case GT:
                    return comparisonResult > 0;
                case LT:
                    return comparisonResult < 0;
                case GTE:
                    return comparisonResult >= 0;
                case LTE:
                    return comparisonResult <= 0;
                case EQ:
                    return comparisonResult == 0;
            }

            return false;
        }

        private Instant parseDateTime(String rawValue) {
            return LuceneUtils.stringToDate(rawValue).toInstant();
        }
    }

    private class DateComparisonQueryListener extends DateComparisonQueryBaseListener {
        private final Locale userLocale;

        private boolean isLeft = true;
        private String leftField;
        private boolean isLeftDate;
        private long leftModifier = 0;
        private boolean isLeftPositive = false;
        private String rightField;
        private boolean isRightDate;
        private long rightModifier = 0;
        private boolean isRightPositive = false;
        private DateCompareOperation operation;
        private boolean complete;

        private DateComparisonQueryListener(Locale userLocale) {
            this.userLocale = userLocale;
        }

        @Override
        public void exitSign_expr(DateComparisonQueryParser.Sign_exprContext ctx) {
            boolean isPositive = "+".equals(AntlrUtil.unescapeString(ctx.getText()));
            if (isLeft) {
                isLeftPositive = isPositive;
            } else {
                isRightPositive = isPositive;
            }
        }

        @Override
        public void exitDate_field_expr(DateComparisonQueryParser.Date_field_exprContext ctx) {
            String fieldName = AntlrUtil.unescapeString(ctx.getText());

            Field field = fieldManager.getField(fieldName);

            if (field == null) {
                Collection<CustomField> fieldsByName = customFieldManager.getCustomFieldObjectsByName(fieldName);

                if (fieldsByName.size() > 1) {
                    throw new RuntimeException(
                        "Found multiple fields by name \"" + fieldName + "\": " +
                            fieldsByName.stream().map(CustomField::getId).collect(Collectors.joining(", "))
                    );
                }

                if (fieldsByName.size() == 1) {
                    field = fieldsByName.iterator().next();
                }
            }

            if (field == null) {
                throw new RuntimeException("Unknown field");
            }

            if (!(field instanceof DateField)) {
                throw new RuntimeException("Field " + field + " is not date field");
            }

            boolean isDateTimeField;
            if (field instanceof CustomField) {
                Optional<String> searcherOptional = FieldUtil.getSearcherKey((CustomField) field);

                if (searcherOptional.isPresent()) {
                    switch (searcherOptional.get()) {
                        case Const.SEARCHER_DATE:
                            isDateTimeField = true;
                            break;
                        case Const.SEARCHER_DATETIME:
                            isDateTimeField = false;
                            break;
                        default:
                            throw new RuntimeException("Not a date field: " + fieldName);
                    }
                } else {
                    throw new RuntimeException("Searcher for field " + fieldName + " isn't specified");
                }
            } else {
                isDateTimeField = !Const.SYSTEM_DATE_FIELDS.contains(field.getClass());
            }

            if (isLeft) {
                leftField = field.getId();
                isLeftDate = !isDateTimeField;
            } else {
                rightField = field.getId();
                isRightDate = !isDateTimeField;
            }
        }

        @Override
        public void exitDuration(DateComparisonQueryParser.DurationContext ctx) {
            try {
                String durationStr = AntlrUtil.unescapeString(ctx.getText());

                long duration = DurationUtils.getDurationSeconds(
                    durationStr,
                    TimeUnit.DAYS.toSeconds(1),
                    TimeUnit.DAYS.toSeconds(7),
                    DateUtils.Duration.DAY,
                    userLocale,
                    TOKEN_MAP
                );

                if (isLeft) {
                    leftModifier = duration;
                } else {
                    rightModifier = duration;
                }
            } catch (InvalidDurationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void exitOperation_expr(DateComparisonQueryParser.Operation_exprContext ctx) {
            String operationString = AntlrUtil.unescapeString(ctx.getText());

            operation = DateCompareOperation.fromString(operationString);
            isLeft = false;
        }

        @Override
        public void visitTerminal(TerminalNode node) {
            complete = true;
        }

        public DateCompareQuery buildQuery() {
            if (!complete) {
                throw new IllegalStateException("Parsing is not completed");
            }

            return new DateCompareQuery(
                operation,
                leftField,
                leftModifier * (isLeftPositive ? 1 : -1),
                isLeftDate,
                rightField,
                rightModifier * (isRightPositive ? 1 : -1),
                isRightDate
            );
        }
    }
}
