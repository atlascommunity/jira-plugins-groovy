package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.LikeQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Either;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryBaseListener;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryLexer;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryParser;
import ru.mail.jira.plugins.groovy.util.AntlrUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class CommentedFunction extends AbstractBuiltInFunction {
    private final Logger logger = LoggerFactory.getLogger(CommentedFunction.class);
    private final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false);

    private final SearchProviderFactory searchProviderFactory;
    private final ProjectRoleManager projectRoleManager;
    private final TimeZoneManager timeZoneManager;
    private final ProjectManager projectManager;
    private final JqlDateSupport jqlDateSupport;
    private final GroupManager groupManager;
    private final UserManager userManager;

    @Autowired
    public CommentedFunction(
        @ComponentImport SearchProviderFactory searchProviderFactory,
        @ComponentImport ProjectRoleManager projectRoleManager,
        @ComponentImport TimeZoneManager timeZoneManager,
        @ComponentImport ProjectManager projectManager,
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport GroupManager groupManager,
        @ComponentImport UserManager userManager
    ) {
        super("commented", 1);
        this.searchProviderFactory = searchProviderFactory;
        this.projectRoleManager = projectRoleManager;
        this.timeZoneManager = timeZoneManager;
        this.projectManager = projectManager;
        this.jqlDateSupport = jqlDateSupport;
        this.groupManager = groupManager;
        this.userManager = userManager;
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser user, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        Either<Query, MessageSet> parseResult = parseParameters(user, ImmutableList.of(), functionOperand.getArgs().get(0));

        if (parseResult.isRight()) {
            messageSet.addMessageSet(parseResult.right().get());
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        FunctionOperand functionOperand = (FunctionOperand) terminalClause.getOperand();
        List<String> args = functionOperand.getArgs();

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);

        Either<Query, MessageSet> parseResult = parseParameters(user, queryCreationContext.getDeterminedProjects(), args.get(0));

        if (parseResult.isRight()) {
            logger.error("Got errors while building query: {}", parseResult.right().get());
            return QueryFactoryResult.createFalseResult();
        }

        CommentIssueCollector collector = new CommentIssueCollector();

        try {
            searcher.search(parseResult.left().get(), collector);
        } catch (IOException e) {
            logger.error("caught exception while searching", e);
        }

        return new QueryFactoryResult(new ConstantScoreQuery(new IssueIdFilter(collector.issueIds)));
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(
        @Nonnull QueryCreationContext queryCreationContext,
        @Nonnull FunctionOperand functionOperand,
        @Nonnull TerminalClause terminalClause
    ) {
        return ImmutableList.of();
    }

    private Either<Query, MessageSet> parseParameters(
        ApplicationUser user,
        Collection<String> determinedProjects,
        String queryString
    ) {
        ZoneId userZoneId = timeZoneManager.getTimeZoneforUser(user).toZoneId();

        BooleanQuery query = new BooleanQuery();

        MessageSet messageSet = new MessageSetImpl();

        parseQuery(messageSet, queryString).forEach((key, value) -> {
            switch (key) {
                case "by": {
                    ApplicationUser byUser;

                    if ("currentUser()".equalsIgnoreCase(value)) {
                        byUser = user;
                    } else {
                        byUser = userManager.getUserByName(value);
                        if (byUser == null) {
                            byUser = userManager.getUserByKey(value);
                        }
                    }

                    if (byUser != null) {
                        query.add(new TermQuery(new Term(DocumentConstants.COMMENT_AUTHOR, byUser.getKey())), BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Unable to find user \"" + value + "\"");
                    }
                    break;
                }
                case "like": {
                    query.add(
                        likeQueryFactory.createQueryForSingleValue(
                            DocumentConstants.COMMENT_BODY,
                            Operator.LIKE,
                            ImmutableList.of(new QueryLiteral(new SingleValueOperand(value), value))
                        ).getLuceneQuery(),
                        BooleanClause.Occur.MUST
                    );
                    break;
                }
                case "on": {
                    if (jqlDateSupport.validate(value)) {
                        LocalDate date = jqlDateSupport
                            .convertToDate(value)
                            .toInstant()
                            .atZone(userZoneId)
                            .toLocalDate();

                        Date since = Date.from(date.atStartOfDay(userZoneId).toInstant());
                        Date until = Date.from(date.atTime(LocalTime.MAX).atZone(userZoneId).toInstant());

                        query.add(
                            new TermRangeQuery(
                                DocumentConstants.COMMENT_CREATED,
                                LuceneUtils.dateToString(since), LuceneUtils.dateToString(until),
                                true, true
                            ),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"on\" date is invalid");
                    }
                    break;
                }
                case "before": {
                    if (jqlDateSupport.validate(value)) {
                        Date date = jqlDateSupport.convertToDate(value);

                        query.add(
                            new TermRangeQuery(DocumentConstants.COMMENT_CREATED, null, LuceneUtils.dateToString(date), true, true),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"before\" date is invalid");
                    }
                    break;
                }
                case "after": {
                    if (jqlDateSupport.validate(value)) {
                        Date date = jqlDateSupport.convertToDate(value);

                        query.add(
                            new TermRangeQuery(DocumentConstants.COMMENT_CREATED, LuceneUtils.dateToString(date), null, true, true),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"after\" date is invalid");
                    }
                    break;
                }
                case "inRole": {
                    List<Project> projects;

                    if (determinedProjects.size() > 0) {
                        //todo: use projectManager.getProjectsByArgs when it will be safe to support only Jira >= 7.10.x
                        projects = new ArrayList<>();
                        for (String idOrKeyOrName : determinedProjects) {
                            Project project;
                            long id = NumberUtils.toLong(idOrKeyOrName, -1L);
                            if (id > -1) {
                                project = projectManager.getProjectObj(id);
                            } else {
                                project = projectManager.getProjectObjByKey(idOrKeyOrName);
                                if (project == null) {
                                    project = projectManager.getProjectObjByName(idOrKeyOrName);
                                }
                            }

                            if (project != null) {
                                projects.add(project);
                            } else {
                                logger.warn("unable to find determined project for string \"{}\"", idOrKeyOrName);
                            }
                        }
                        projects = new ArrayList<>();
                    } else {
                        projects = projectManager.getProjects();
                    }

                    ProjectRole role = projectRoleManager.getProjectRole(value);

                    if (role != null) {
                        BooleanQuery projectsQuery = new BooleanQuery();
                        for (Project project : projects) {
                            //todo: do something to safely check if project is archived for Jira prior to 7.10.x
/*                            if (project.isArchived()) {
                                logger.warn("ignoring archived project {}", project.getKey());
                                continue;
                            }*/

                            ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(role, project);

                            if (projectRoleActors != null) {
                                BooleanQuery projectQuery = new BooleanQuery();
                                projectQuery.add(
                                    new TermQuery(new Term(DocumentConstants.PROJECT_ID, String.valueOf(project.getId()))),
                                    BooleanClause.Occur.MUST
                                );

                                BooleanQuery usersQuery = new BooleanQuery();
                                for (ApplicationUser roleUser : projectRoleActors.getApplicationUsers()) {
                                    usersQuery.add(
                                        new TermQuery(new Term(DocumentConstants.COMMENT_AUTHOR, roleUser.getKey())),
                                        BooleanClause.Occur.SHOULD
                                    );
                                }
                                projectQuery.add(usersQuery, BooleanClause.Occur.MUST);

                                projectsQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
                            }
                        }
                        query.add(projectsQuery, BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Role \"" + value + "\" wasn't found");
                    }
                    break;
                }
                case "inGroup": {
                    Group group = groupManager.getGroup(value);

                    if (group != null) {
                        BooleanQuery groupQuery = new BooleanQuery();

                        for (ApplicationUser groupUser : groupManager.getUsersInGroup(group)) {
                            groupQuery.add(
                                new TermQuery(new Term(DocumentConstants.COMMENT_AUTHOR, groupUser.getKey())),
                                BooleanClause.Occur.SHOULD
                            );
                        }
                        query.add(groupQuery, BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Group \"" + value + "\" wasn't found");
                    }
                    break;
                }
                case "roleLevel": {
                    ProjectRole role = projectRoleManager.getProjectRole(value);
                    if (role != null) {
                        query.add(new TermQuery(new Term(
                            DocumentConstants.COMMENT_LEVEL_ROLE, String.valueOf(role.getId())
                        )), BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Role \"" + value + "\" wasn't found");
                    }
                    break;
                }
                case "groupLevel": {
                    Group group = groupManager.getGroup(value);

                    if (group != null) {
                        query.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, value)), BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Group \"" + value + "\" wasn't found");
                    }
                    break;
                }
            }
        });

        if (messageSet.hasAnyErrors()) {
            return Either.right(messageSet);
        }

        return Either.left(query);
    }

    private Map<String, String> parseQuery(MessageSet messageSet, String query) {
        CommentedQueryLexer lexer = new CommentedQueryLexer(CharStreams.fromString(query));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        CommentedQueryParser parser = new CommentedQueryParser(tokenStream);

        CommentedQueryListener listener = new CommentedQueryListener();
        parser.addParseListener(listener);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                messageSet.addErrorMessage("Parsing error at " + charPositionInLine + ": " + msg);
            }
        });

        parser.commented_query();

        if (logger.isDebugEnabled()) {
            logger.debug("Parsed values {}", listener.values);
        }

        return listener.values;
    }

    private static class CommentIssueCollector extends Collector {
        private final Set<String> issueIds = new HashSet<>();

        private IndexReader indexReader;

        @Override
        public void setScorer(Scorer scorer) {

        }

        @Override
        public void collect(int i) throws IOException {
            Document document = indexReader.document(i);

            String issueId = document.get(DocumentConstants.ISSUE_ID);

            if (issueId != null) {
                System.out.println(document.getFields());
                issueIds.add(issueId);
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

    private static class CommentedQueryListener extends CommentedQueryBaseListener {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public void exitBy_query(CommentedQueryParser.By_queryContext ctx) {
            values.put("by", AntlrUtil.unescapeString(ctx.username_expr().getText()));
        }

        @Override
        public void exitLike_query(CommentedQueryParser.Like_queryContext ctx) {
            values.put("like", AntlrUtil.unescapeString(ctx.str_expr().getText()));
        }

        @Override
        public void exitDate_query(CommentedQueryParser.Date_queryContext ctx) {
            values.put(ctx.date_field().getText(), AntlrUtil.unescapeString(ctx.date_expr().getText()));
        }

        @Override
        public void exitGroup_query(CommentedQueryParser.Group_queryContext ctx) {
            values.put(ctx.group_field().getText(), AntlrUtil.unescapeString(ctx.group_expr().getText()));
        }

        @Override
        public void exitRole_query(CommentedQueryParser.Role_queryContext ctx) {
            values.put(ctx.role_field().getText(), AntlrUtil.unescapeString(ctx.role_expr().getText()));
        }
    }
}
