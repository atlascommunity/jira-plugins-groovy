package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.jsw;

import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.AbstractBuiltInFunction;
import ru.mail.jira.plugins.groovy.impl.jsw.JiraSoftwareHelper;

import javax.annotation.Nonnull;
import java.util.*;

@Scanned
public abstract class AbstractSprintHistoryFunction extends AbstractBuiltInFunction {
    private final Logger logger = LoggerFactory.getLogger(AbstractSprintHistoryFunction.class);

    private final SearchProvider searchProvider;
    private final JiraSoftwareHelper jiraSoftwareHelper;
    private final boolean added;

    public AbstractSprintHistoryFunction(
        @ComponentImport SearchProvider searchProvider,
        JiraSoftwareHelper jiraSoftwareHelper,
        String functionName,
        boolean added
    ) {
        super(functionName, 1);
        this.searchProvider = searchProvider;
        this.jiraSoftwareHelper = jiraSoftwareHelper;
        this.added = added;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        if (!jiraSoftwareHelper.isAvailable()) {
            messageSet.addErrorMessage("Jira Software is not available");
        }

        List<String> args = functionOperand.getArgs();

        if (args.size() > 2) {
            messageSet.addErrorMessage("Too many arguments");
            return;
        }

        String boardName = args.get(0);
        RapidView rapidView = jiraSoftwareHelper.findRapidViewByName(user, boardName);

        if (rapidView == null) {
            messageSet.addErrorMessage("Can't find scrum board with name \"" + boardName + "\"");
            return;
        }

        if (args.size() == 2) {
            String sprintName = args.get(1);

            Sprint sprint = jiraSoftwareHelper.findSprint(user, rapidView, sprintName);

            if (sprint == null) {
                messageSet.addErrorMessage("Can't find sprint with name \"" + sprintName + "\" in board \"" + rapidView.getName() + "\"");
            }
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        if (!jiraSoftwareHelper.isAvailable()) {
            return QueryFactoryResult.createFalseResult();
        }

        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        jiraSoftwareHelper.getSprintField();

        CustomField sprintField = jiraSoftwareHelper.getSprintField();

        RapidView rapidView = jiraSoftwareHelper.findRapidViewByName(user, args.get(0));

        if (rapidView == null) {
            logger.warn("Unable to find rapid view for name \"{}\"", args.get(0));

            return QueryFactoryResult.createFalseResult();
        }

        com.atlassian.query.Query rapidViewQuery = JqlQueryBuilder
            .newBuilder(jiraSoftwareHelper.getRapidViewQuery(user, rapidView))
            .where().and().not().issueTypeIsSubtask().buildQuery();

        Map<Long, DateTime> startDates = new HashMap<>();

        String historicFieldId = sprintField.getId() + "_history";

        Query luceneQuery = null;

        if (args.size() == 1) {
            BooleanQuery sprintsQuery = new BooleanQuery();

            for (Sprint sprint : jiraSoftwareHelper.findActiveSprintsByBoard(user, rapidView)) {
                if (sprint.getStartDate() != null) {
                    startDates.put(sprint.getId(), sprint.getStartDate());

                    sprintsQuery.add(new TermQuery(new Term(historicFieldId, String.valueOf(sprint.getId()))), BooleanClause.Occur.SHOULD);
                }
            }

            luceneQuery = sprintsQuery;
        } else if (args.size() == 2) {
            Sprint sprint = jiraSoftwareHelper.findSprint(user, rapidView, args.get(1));

            if (sprint == null) {
                logger.warn("Unable to find sprint with name \"{}\" in board \"{}\"", args.get(1), args.get(0));

                return QueryFactoryResult.createFalseResult();
            }

            luceneQuery = new TermQuery(new Term(historicFieldId, String.valueOf(sprint.getId())));
        }

        SprintHistoryCollector collector = new SprintHistoryCollector(sprintField, startDates, added);
        try {
            searchProvider.search(rapidViewQuery, user, collector, luceneQuery);
        } catch (SearchException e) {
            logger.error("error while searching", e);
        }

        return new QueryFactoryResult(
            new ConstantScoreQuery(new IssueIdFilter(collector.getIssues())),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

}
