package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectConstant;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import ru.mail.jira.plugins.groovy.util.lucene.IssueIdCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Component
public class IssueFieldMatch extends AbstractBuiltInQueryFunction {
    private final Logger logger = LoggerFactory.getLogger(IssueFieldMatch.class);
    private final FieldManager fieldManager;
    private final SearchProvider searchProvider;
    private final SearchService searchService;
    private final VersionManager versionManager;
    private final ProjectComponentManager projectComponentManager;
    private final ProjectManager projectManager;

    @Autowired
    public IssueFieldMatch(
        @ComponentImport FieldManager fieldManager,
        @ComponentImport SearchProvider searchProvider,
        @ComponentImport SearchService searchService,
        @ComponentImport VersionManager versionManager,
        @ComponentImport ProjectComponentManager projectComponentManager,
        @ComponentImport ProjectManager projectManager
    ) {
        super("issueFieldMatch", 3);
        this.fieldManager = fieldManager;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
        this.versionManager = versionManager;
        this.projectComponentManager = projectComponentManager;
        this.projectManager = projectManager;
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        Query query = getQuery(applicationUser, args.get(0), messageSet);

        if (query == null) {
            return;
        }

        Field field = fieldManager.getField(args.get(1));

        if (field == null) {
            messageSet.addErrorMessage("Unknown field \"" + args.get(1) + "\"");
        }

        try {
            Pattern.compile(args.get(2));
        } catch (PatternSyntaxException e) {
            messageSet.addErrorMessage(e.getMessage());
        }

    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = operand.getArgs();

        String queryString = args.get(0);
        String field = args.get(1);
        String patternString = args.get(2);

        Query query = getQuery(user, queryString, null);

        if (query == null) {
            logger.warn("invalid query");
            return QueryFactoryResult.createFalseResult();
        }

        String indexField = field;

        ClauseInformation clauseInformation = SystemSearchConstants.getClauseInformationById(field);
        if (clauseInformation != null) {
            indexField = clauseInformation.getIndexField();
        }

        Pattern pattern = Pattern.compile(patternString);

        Set<String> entityIds = null;
        if (DocumentConstants.ISSUE_FIXVERSION.equals(indexField) || DocumentConstants.ISSUE_VERSION.equals(indexField)) {
            entityIds = versionManager
                .getAllVersions()
                .stream()
                .filter(it -> pattern.matcher(it.getName()).find())
                .map(ProjectConstant::getId)
                .map(Objects::toString)
                .collect(Collectors.toSet());
        } else if (DocumentConstants.ISSUE_COMPONENT.equals(indexField)) {
            entityIds = projectComponentManager
                .findAll()
                .stream()
                .filter(it -> pattern.matcher(it.getName()).find())
                .map(ProjectConstant::getId)
                .map(Objects::toString)
                .collect(Collectors.toSet());
        } else if (DocumentConstants.PROJECT_ID.equals(indexField)) {
            entityIds = projectManager
                .getProjects()
                .stream()
                .filter(it -> pattern.matcher(it.getName()).find())
                .map(Project::getId)
                .map(Objects::toString)
                .collect(Collectors.toSet());
        }

        if (entityIds != null) {
            BooleanQuery booleanQuery = new BooleanQuery();

            for (String entityId : entityIds) {
                booleanQuery.add(new TermQuery(new Term(indexField, entityId)), BooleanClause.Occur.SHOULD);
            }

            IssueIdCollector issueIdCollector = new IssueIdCollector();

            try {
                searchProvider.search(query, user, issueIdCollector, booleanQuery);
            } catch (SearchException e) {
                logger.error("Caught exception while searching", e);
            }

            return new QueryFactoryResult(
                new ConstantScoreQuery(new IssueIdFilter(issueIdCollector.getIssueIds())),
                terminalClause.getOperator() == Operator.NOT_IN
            );
        }

        PatternCollector collector = new PatternCollector(pattern, indexField);

        try {
            searchProvider.search(query, user, collector);
        } catch (SearchException e) {
            logger.warn("Caught exception while searching", e);
        }

        return new QueryFactoryResult(
            new ConstantScoreQuery(new IssueIdFilter(collector.issueIds)),
            terminalClause.getOperator() == Operator.NOT_IN
        );
    }

    private Query getQuery(ApplicationUser user, String queryString, MessageSet messageSet) {
        SearchService.ParseResult queryResult = searchService.parseQuery(user, queryString);

        if (!queryResult.isValid()) {
            if (messageSet != null) {
                messageSet.addMessageSet(queryResult.getErrors());
            } else {
                logger.error("\"{}\" query is not valid {}", queryString, queryResult.getErrors());
            }

            return null;
        }

        return queryResult.getQuery();
    }

    private static class PatternCollector extends Collector {
        private final Set<String> issueIds = new HashSet<>();

        private final Pattern pattern;
        private final String field;
        private final FieldSelector fieldSelector;

        private IndexReader indexReader;

        private PatternCollector(Pattern pattern, String field) {
            this.pattern = pattern;
            this.field = field;
            this.fieldSelector = new SetBasedFieldSelector(
                ImmutableSet.of(
                    field, DocumentConstants.ISSUE_ID
                ),
                ImmutableSet.of()
            );
        }

        @Override
        public void setScorer(Scorer scorer) {

        }

        @Override
        public void collect(int i) throws IOException {
            Document document = indexReader.document(i, fieldSelector);

            for (String value : document.getValues(field)) {
                if (value != null) {
                    if (pattern.matcher(value).find()) {
                        issueIds.add(document.get(DocumentConstants.ISSUE_ID));
                    }
                }
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
    }
}
