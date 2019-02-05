package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.jsw;

import com.atlassian.greenhopper.model.rapid.RapidView;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.AbstractBuiltInQueryFunction;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.SearchHelper;
import ru.mail.jira.plugins.groovy.impl.jsw.JiraSoftwareHelper;
import ru.mail.jira.plugins.groovy.util.lucene.QueryUtil;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class AbstractSprintHistoryFunction extends AbstractBuiltInQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(AbstractSprintHistoryFunction.class);

    private final JiraSoftwareHelper jiraSoftwareHelper;
    private final SearchHelper searchHelper;
    private final boolean added;

    public AbstractSprintHistoryFunction(
        JiraSoftwareHelper jiraSoftwareHelper,
        SearchHelper searchHelper,
        String functionName, boolean added
    ) {
        super(functionName, 1);
        this.jiraSoftwareHelper = jiraSoftwareHelper;
        this.searchHelper = searchHelper;
        this.added = added;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        if (!jiraSoftwareHelper.isAvailable()) {
            messageSet.addErrorMessage("Jira Software is not available");
            return;
        }

        List<String> args = functionOperand.getArgs();

        if (args.size() > 2) {
            messageSet.addErrorMessage("Too many arguments");
            return;
        }

        String boardName = args.get(0);
        Optional<RapidView> rapidView = jiraSoftwareHelper.findRapidViewByName(user, boardName);

        if (!rapidView.isPresent()) {
            messageSet.addErrorMessage("Can't find scrum board with name \"" + boardName + "\"");
            return;
        }

        if (args.size() == 2) {
            String sprintName = args.get(1);

            Optional<Sprint> sprint = jiraSoftwareHelper.findSprint(user, rapidView, sprintName);

            if (!sprint.isPresent()) {
                messageSet.addErrorMessage("Can't find sprint with name \"" + sprintName + "\" in board \"" + rapidView.get().getName() + "\"");
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

        Optional<RapidView> rapidView = jiraSoftwareHelper.findRapidViewByName(user, args.get(0));

        if (!rapidView.isPresent()) {
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
            BooleanQuery.Builder sprintsQuery = new BooleanQuery.Builder();

            for (Sprint sprint : jiraSoftwareHelper.findActiveSprintsByBoard(user, rapidView)) {
                if (sprint.getStartDate() != null) {
                    startDates.put(sprint.getId(), sprint.getStartDate());

                    sprintsQuery.add(new TermQuery(new Term(historicFieldId, String.valueOf(sprint.getId()))), BooleanClause.Occur.SHOULD);
                }
            }

            luceneQuery = sprintsQuery.build();
        } else if (args.size() == 2) {
            Optional<Sprint> sprint = jiraSoftwareHelper.findSprint(user, rapidView, args.get(1));

            if (!sprint.isPresent()) {
                logger.warn("Unable to find sprint with name \"{}\" in board \"{}\"", args.get(1), args.get(0));

                return QueryFactoryResult.createFalseResult();
            }

            luceneQuery = new TermQuery(new Term(historicFieldId, String.valueOf(sprint.get().getId())));
        }

        SprintHistoryCollector collector = new SprintHistoryCollector(sprintField, startDates, added);

        searchHelper.doSearch(rapidViewQuery, luceneQuery, collector, queryCreationContext);

        return new QueryFactoryResult(
            QueryUtil.createIssueIdQuery(collector.getIssues()),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

}
