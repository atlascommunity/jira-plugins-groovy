package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.DurationUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.*;
import ru.mail.jira.plugins.groovy.util.AntlrUtil;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.FieldUtil;
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

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
    private final TimeZoneManager timeZoneManager;
    private final CustomFieldManager customFieldManager;
    private final SearchHelper searchHelper;

    @Autowired
    public DateCompareFunction(
        @ComponentImport FieldManager fieldManager,
        @ComponentImport LocaleManager localeManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport CustomFieldManager customFieldManager,
        SearchHelper searchHelper
    ) {
        super("dateCompare", 2);
        this.fieldManager = fieldManager;
        this.localeManager = localeManager;
        this.timeZoneManager = timeZoneManager;
        this.customFieldManager = customFieldManager;
        this.searchHelper = searchHelper;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        List<String> args = operand.getArgs();

        String queryString = args.get(0);
        String compareQueryString = args.get(1);

        searchHelper.validateJql(messageSet, user, queryString);

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

        Query query = searchHelper.getQuery(user, queryString);

        DateCompareQuery dateCompareQuery = parseQuery(messageSet, user, compareQueryString);

        if (messageSet.hasAnyErrors()) {
            logger.error("Has errors {}", messageSet.getErrorMessages());
            return QueryFactoryResult.createFalseResult();
        }

        DateCompareCollector collector = new DateCompareCollector(
            dateCompareQuery,
            timeZoneManager.getTimeZoneforUser(user).toZoneId()
        );

        BooleanQuery.Builder luceneQuery = new BooleanQuery.Builder();
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

        searchHelper.doSearch(query, luceneQuery.build(), collector, queryCreationContext);

        return new QueryFactoryResult(
            QueryUtil.createIssueIdQuery(collector.issueIds),
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

        try {
            parser.date_comparison_query();
        } catch (Exception e) {
            messageSet.addErrorMessage(e.getMessage());
        }

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

    private static class DateCompareCollector extends SimpleCollector {
        private final Set<String> issueIds = new HashSet<>();
        private final DateCompareQuery query;
        private final ZoneId userTz;
        private NumericDocValues leftFieldValues;
        private NumericDocValues rightFieldValues;
        private SortedDocValues issueIdValues;

        private DateCompareCollector(DateCompareQuery query, ZoneId userTz) {
            this.query = query;
            this.userTz = userTz;
        }

        @Override
        public void doSetNextReader(LeafReaderContext context) throws IOException {
            issueIdValues = context.reader().getSortedDocValues(DocumentConstants.ISSUE_ID);
            leftFieldValues = context.reader().getNumericDocValues(query.getLeftField());
            rightFieldValues = context.reader().getNumericDocValues(query.getRightField());
        }

        @Override
        public void collect(int doc) throws IOException {
            if (leftFieldValues.advanceExact(doc) && rightFieldValues.advanceExact(doc)) {
                long rawLeftValue = leftFieldValues.longValue();
                long rawRightValue = rightFieldValues.longValue();

                Instant leftInstant = (query.isLeftDate() ? parseLocalDate(rawLeftValue) : parseDateTime(rawLeftValue)).plusSeconds(query.leftModifier);
                Instant rightInstant = (query.isRightDate() ? parseLocalDate(rawRightValue) : parseDateTime(rawRightValue)).plusSeconds(query.rightModifier);

                if (compare(leftInstant.compareTo(rightInstant))) {
                    if (issueIdValues.advanceExact(doc)) {
                        issueIds.add(issueIdValues.binaryValue().utf8ToString());
                    }
                }
            }
        }

        private Instant parseLocalDate(long rawValue) {
            LocalDate localDate = new LocalDate(rawValue);

            return java.time.LocalDate
                .of(localDate.getYear(), localDate.getMonth(), localDate.getDay())
                .atTime(LocalTime.MIN)
                .atZone(userTz)
                .toInstant();
        }

        private Instant parseDateTime(long rawValue) {
            return Instant.ofEpochMilli(rawValue);
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

        @Override
        public boolean needsScores() {
            return false;
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
                field = fieldManager.getField(fieldName.toLowerCase());
            }

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
                throw new RuntimeException("Unknown field: " + fieldName);
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
