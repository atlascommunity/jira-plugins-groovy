package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.searchers.DateRangeSearcher;
import com.atlassian.jira.issue.customfields.searchers.DateTimeRangeSearcher;
import com.atlassian.jira.issue.customfields.searchers.ExactNumberSearcher;
import com.atlassian.jira.issue.customfields.searchers.NumberRangeSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.AbstractBuiltInQueryFunction;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.DateCompareFunction;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.SearchHelper;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ExpressionFunction extends AbstractBuiltInQueryFunction {
    private final Logger log = LoggerFactory.getLogger(DateCompareFunction.class);

    private final SearchHelper searchHelper;
    private final JqlOperandResolver jqlOperandResolver;
    private final JqlQueryParser jqlQueryParser;
    private final SearchHandlerManager searchHandlerManager;
    private final CustomFieldManager customFieldManager;
    private final GroovyClassLoader groovyClassLoader;
    private final GroovyShell groovyShell;
    private final AllVariableExpressionsVisitor visitor = new AllVariableExpressionsVisitor();
    private final DoubleConverter doubleConverter;

    @Autowired
    public ExpressionFunction(SearchHelper searchHelper, @ComponentImport JqlOperandResolver jqlOperandResolver, @ComponentImport JqlQueryParser jqlQueryParser, @ComponentImport CustomFieldManager customFieldManager, @ComponentImport SearchHandlerManager searchHandlerManager, @ComponentImport DoubleConverter doubleConverter) {
        super("expression", 2);
        this.searchHelper = searchHelper;
        this.jqlOperandResolver = jqlOperandResolver;
        this.jqlQueryParser = jqlQueryParser;
        this.searchHandlerManager = searchHandlerManager;
        this.customFieldManager = customFieldManager;
        this.doubleConverter = doubleConverter;


        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(new AllVariablesMemorizerExtension(visitor));

        groovyClassLoader = new GroovyClassLoader(ClassLoaderUtil.getCurrentPluginClassLoader(), compilerConfiguration);

        // TODO bindings for jql functions calls and dates short values like: wd, 2w, 3d etc...
        groovyShell = new GroovyShell(groovyClassLoader, new Binding());

        // TODO
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("AdditionalArithmeticOperators.groovy");
        if (resourceAsStream == null) {
            log.error("AdditionalArithmeticOperators.groovy script not found in resources folder" +
                      "This may lead to some arithmetic operators inside expressions clause not working");
        } else {
            Script additionalOperatorsScript = groovyShell.parse(new InputStreamReader(resourceAsStream));
            additionalOperatorsScript.run();
        }
    }


    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return null;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand operand, @Nonnull TerminalClause clause) {
        List<String> args = operand.getArgs();
        if (args.size() != 2) {
            messageSet.addWarningMessage("Usage: expression(\"subQuery\", \"expressionQuery\")");
        }

        String subQueryStr = args.get(0);
        searchHelper.validateJql(messageSet, user, subQueryStr);
        // TODO validate expressions query str
    }


    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        Operand operand = terminalClause.getOperand();
        if (!jqlOperandResolver.isValidOperand(operand) || !(operand instanceof FunctionOperand)) {
            //TODO correct warning message in log
            return QueryFactoryResult.createFalseResult();
        }
        // we are here always when groovyFunction in expressions(arg1, arg2) called
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();
        if (args.size() != 2) {
            // c'mon jira how does it passed validation stage?
            //TODO log
            return QueryFactoryResult.createFalseResult();
        }

        Query subQuery = searchHelper.getQuery(user, args.get(0));
        String expressionsQueryStr = args.get(1);

        groovyClassLoader.parseClass(expressionsQueryStr);
        Set<String> allUsedFieldNames = visitor.getFields();
        // String fieldName -> ClauseInformation mapping
        Map<String, ClauseInformation> allFieldsClauseInfos = findAllClauseInfos(allUsedFieldNames);
        Query nonEmptyFieldsQuery = buildNonEmptyFieldsQuery(subQuery, allFieldsClauseInfos.values());

        // set of fields to load in lucene document
        Set<String> fieldsToLoadFromIndex = allFieldsClauseInfos.values().stream().map(ClauseInformation::getIndexField).collect(Collectors.toSet());
        fieldsToLoadFromIndex.add(SystemSearchConstants.forIssueId().getIndexField());
        Map<String, LuceneFieldValueExtractor> extractorMap = new HashMap<>(allFieldsClauseInfos.size(), 1);
        allFieldsClauseInfos.forEach((fieldName, clauseInformation) -> {
            extractorMap.put(fieldName, retrieveFieldValueExtractor(clauseInformation));
        });

        Script parsedScript = groovyShell.parse(expressionsQueryStr);
        ExpressionIssueIdCollector expressionIssueIdCollector = new ExpressionIssueIdCollector(fieldsToLoadFromIndex, parsedScript, extractorMap);
        searchHelper.doSearch(nonEmptyFieldsQuery, null, expressionIssueIdCollector, queryCreationContext);

        visitor.clear();
        return new QueryFactoryResult(
                QueryUtil.createIssueIdQuery(expressionIssueIdCollector.getIssueIds()),
                terminalClause.getOperator() == Operator.NOT_IN
        );
    }


    private Query buildNonEmptyFieldsQuery(Query query, Collection<ClauseInformation> clauseInfoList) {
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder(query.getWhereClause()).defaultAnd();
        clauseInfoList.forEach(clauseInformation -> {
            String fieldId = clauseInformation.getFieldId();
            if (fieldId != null && fieldId.startsWith("customfield_")) {
                String cfId = fieldId.replaceAll("customfield_", "");
                jqlClauseBuilder.addClause(JqlQueryBuilder.newClauseBuilder().customField(Long.valueOf(cfId)).isNotEmpty().buildClause());
            } else {
                jqlClauseBuilder.addClause(JqlQueryBuilder.newClauseBuilder().field(clauseInformation.getJqlClauseNames().getPrimaryName()).isNotEmpty().buildClause());
            }
        });
        return jqlClauseBuilder.buildQuery();
    }

    public Map<String, ClauseInformation> findAllClauseInfos(Set<String> fieldNames) {
        List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects();
        Map<String, ClauseInformation> result = new HashMap<>(fieldNames.size(), 1);
        for (String fieldName : fieldNames) {
            Collection<ClauseHandler> clauseHandler = searchHandlerManager.getClauseHandler(fieldName);
            if (clauseHandler.size() > 0) {
                result.put(fieldName, clauseHandler.iterator().next().getInformation());
                continue;
            }

            // if field is a customfield
            Optional<CustomField> cfOptional = customFieldObjects.stream().filter(cf -> isFieldEqualsCf(fieldName, cf)).findFirst();
            if (!cfOptional.isPresent())
                // TODO correctly show the reason
                throw new RuntimeException("SOMETHING REALLY BAD HAPPENED");
            CustomField foundField = cfOptional.get();
            if (!searcherIsSupported(foundField))
                // TODO correctly show the reason
                throw new RuntimeException("SOMETHING REALLY BAD HAPPENED");
            result.put(fieldName, new SimpleFieldSearchConstants(foundField.getId(), OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.ALL));
        }
        return result;
    }

    private boolean isFieldEqualsCf(String fieldName, CustomField customField) {
        String lowerCasedFieldName = fieldName.toLowerCase();
        String lowerCasedCFName = customField.getName().replaceAll("\\s+", "").toLowerCase();
        return lowerCasedFieldName.equals(lowerCasedCFName) || lowerCasedCFName.equals(customField.getId());
    }

    private boolean searcherIsSupported(CustomField cf) {
        CustomFieldSearcher customFieldSearcher = cf.getCustomFieldSearcher();
        return customFieldSearcher instanceof ExactNumberSearcher ||
                customFieldSearcher instanceof NumberRangeSearcher ||
                customFieldSearcher instanceof DateTimeRangeSearcher ||
                customFieldSearcher instanceof DateRangeSearcher;
    }

    @Nullable
    public LuceneFieldValueExtractor retrieveFieldValueExtractor(ClauseInformation clauseInfo) {
        String fieldId = clauseInfo.getFieldId();
        if (fieldId == null)
            return null;

        if (fieldId.startsWith("customfield_")) {
            CustomField cf = customFieldManager.getCustomFieldObject(clauseInfo.getFieldId());
            if (cf == null) {
                // TODO fix this !!!
                throw new IllegalArgumentException("custom field used in expressions query doesn't exist");
            }
            CustomFieldSearcher cfSearcher = cf.getCustomFieldSearcher();
            if (cfSearcher instanceof NumberRangeSearcher || cfSearcher instanceof ExactNumberSearcher) {
                return new DoubleCFExtractor(clauseInfo.getIndexField(), doubleConverter);
            } else if (cfSearcher instanceof DateRangeSearcher) {
                return new DateExtractor(clauseInfo.getIndexField());
            } else if (cfSearcher instanceof DateTimeRangeSearcher) {
                return new DateTimeExtractor(clauseInfo.getIndexField());
            } else {
                // TODO fix this !!!
                throw new IllegalArgumentException("custom field used in expressions query doesn't support");
            }
        }
        switch (fieldId) {
            case IssueFieldConstants.DUE_DATE:
                return new DateExtractor(clauseInfo.getIndexField());
            //ourRetriever = new LocalDateRetriever(this, IssueFieldConstants.DUE_DATE)
            case IssueFieldConstants.CREATED:
            case IssueFieldConstants.UPDATED:
            case IssueFieldConstants.RESOLUTION_DATE:
                //2
                //ourRetriever = new DatetimeRetriever(this, clause.indexField)
                return new DateTimeExtractor(clauseInfo.getIndexField());
            case IssueFieldConstants.TIME_SPENT:
            case IssueFieldConstants.TIME_ESTIMATE:
            case IssueFieldConstants.TIME_ORIGINAL_ESTIMATE:
            case IssueFieldConstants.AGGREGATE_TIME_SPENT:
            case IssueFieldConstants.AGGREGATE_TIME_ESTIMATE:
            case IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE:
                //3
                // def retriever = new MillisecondRetriever(this, clause.fieldId)
                return new MillisecondsExtractor(clauseInfo.getIndexField());
            case IssueFieldConstants.CREATOR:
            case IssueFieldConstants.REPORTER:
            case IssueFieldConstants.ASSIGNEE:
            case IssueFieldConstants.WATCHERS:
                //4
                if (clauseInfo instanceof UserFieldSearchConstantsWithEmpty) {
                    String emptyIndexValue = ((UserFieldSearchConstantsWithEmpty) clauseInfo).getEmptyIndexValue();
                    return new UserExtractor(clauseInfo.getIndexField(), emptyIndexValue);
                }
                return new UserExtractor(clauseInfo.getIndexField(), null);
            case IssueFieldConstants.VOTES:
                //5
                return new DoubleExtractor(clauseInfo.getIndexField());
            case IssueFieldConstants.WORKRATIO:
                //6
                return new WorkRatioExtractor(clauseInfo.getIndexField());
        }
        return null;
    }
}

