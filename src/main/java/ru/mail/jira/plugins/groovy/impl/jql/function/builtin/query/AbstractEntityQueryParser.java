package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.parser.antlr.JqlLexer;
import com.atlassian.jira.jql.parser.antlr.JqlParser;
import com.atlassian.jira.jql.query.LikeQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryBaseListener;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryLexer;
import ru.mail.jira.plugins.groovy.impl.jql.antlr.CommentedQueryParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.AbstractCommentQueryFunction;
import ru.mail.jira.plugins.groovy.util.AntlrUtil;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;
import ru.mail.jira.plugins.groovy.util.compat.ArchivingHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEntityQueryParser {
    private final Logger logger = LoggerFactory.getLogger(AbstractCommentQueryFunction.class);
    private final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false);

    private final JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry;
    private final ProjectRoleManager projectRoleManager;
    private final TimeZoneManager timeZoneManager;
    private final ProjectManager projectManager;
    private final JqlDateSupport jqlDateSupport;
    private final UserKeyService userKeyService;
    private final GroupManager groupManager;
    private final UserManager userManager;

    private final ArchivingHelper archivingHelper;

    private final boolean isLocalDate;
    private final String createdField;
    private final String authorField;
    private final String bodyField;
    private final String levelField;
    private final String roleLevelField;

    protected AbstractEntityQueryParser(
        JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry,
        ProjectRoleManager projectRoleManager,
        TimeZoneManager timeZoneManager,
        ProjectManager projectManager,
        JqlDateSupport jqlDateSupport,
        UserKeyService userKeyService,
        GroupManager groupManager,
        UserManager userManager,
        ArchivingHelper archivingHelper,
        boolean isLocalDate, String createdField, String authorField, String bodyField,
        String levelField, String roleLevelField
    ) {
        this.jqlFunctionHandlerRegistry = jqlFunctionHandlerRegistry;
        this.projectRoleManager = projectRoleManager;
        this.timeZoneManager = timeZoneManager;
        this.projectManager = projectManager;
        this.jqlDateSupport = jqlDateSupport;
        this.userKeyService = userKeyService;
        this.groupManager = groupManager;
        this.userManager = userManager;

        this.archivingHelper = archivingHelper;

        this.isLocalDate = isLocalDate;
        this.createdField = createdField;
        this.authorField = authorField;
        this.bodyField = bodyField;
        this.levelField = levelField;
        this.roleLevelField = roleLevelField;
    }

    public QueryParseResult parseParameters(
        QueryCreationContext queryCreationContext,
        String queryString
    ) {
        ApplicationUser user = queryCreationContext.getApplicationUser();
        ZoneId userZoneId = timeZoneManager.getTimeZoneforUser(user).toZoneId();

        BooleanQuery query = new BooleanQuery();

        MessageSet messageSet = new MessageSetImpl();

        parseQuery(messageSet, queryString).forEach((key, value) -> {
            switch (key) {
                case "by": {
                    Set<String> userKeys = parseUser(queryCreationContext, value);

                    if (userKeys != null && userKeys.size() > 0) {
                        if (userKeys.size() == 1) {
                            query.add(new TermQuery(new Term(authorField, userKeys.iterator().next())), BooleanClause.Occur.MUST);
                        } else {
                            BooleanQuery booleanQuery = new BooleanQuery();
                            for (String userKey : userKeys) {
                                booleanQuery.add(new TermQuery(new Term(authorField, userKey)), BooleanClause.Occur.SHOULD);
                            }
                            query.add(booleanQuery, BooleanClause.Occur.MUST);
                        }
                    } else {
                        messageSet.addErrorMessage("Unable to find user \"" + value + "\"");
                    }
                    break;
                }
                case "like": {
                    query.add(
                        likeQueryFactory.createQueryForSingleValue(
                            bodyField,
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
                                createdField,
                                formatDate(since), formatDate(until),
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
                    Date date = parseDate(queryCreationContext, value);

                    if (date != null) {
                        query.add(
                            new TermRangeQuery(createdField, null, formatDate(date), true, false),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"before\" date is invalid");
                    }
                    break;
                }
                case "after": {
                    Date date = parseDate(queryCreationContext, value);

                    if (date != null) {
                        query.add(
                            new TermRangeQuery(createdField, formatDate(date), null, false, true),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"after\" date is invalid");
                    }
                    break;
                }
                case "inRole": {
                    Collection<String> determinedProjects = queryCreationContext.getDeterminedProjects();

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
                            if (archivingHelper.isProjectArchived(project)) {
                                logger.warn("Project {} is archived", project.getKey());
                                continue;
                            }

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
                                        new TermQuery(new Term(authorField, roleUser.getKey())),
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
                                new TermQuery(new Term(authorField, groupUser.getKey())),
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
                            roleLevelField, String.valueOf(role.getId())
                        )), BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Role \"" + value + "\" wasn't found");
                    }
                    break;
                }
                case "groupLevel": {
                    Group group = groupManager.getGroup(value);

                    if (group != null) {
                        query.add(new TermQuery(new Term(levelField, value)), BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Group \"" + value + "\" wasn't found");
                    }
                    break;
                }
            }
        });

        if (messageSet.hasAnyErrors()) {
            return new QueryParseResult(null, messageSet);
        }

        return new QueryParseResult(addPermissionsCheck(queryCreationContext, query), messageSet);
    }

    protected Map<String, String> parseQuery(MessageSet messageSet, String query) {
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

    private String formatDate(Date date) {
        if (isLocalDate) {
            return LuceneUtils.localDateToString(LocalDateFactory.from(date));
        } else {
            return LuceneUtils.dateToString(date);
        }
    }

    private boolean isFunction(String value) {
        return value.indexOf('(') != -1 && value.indexOf(')') != -1;
    }

    private Set<String> parseUser(QueryCreationContext queryCreationContext, String value) {
        Set<String> result = null;

        if (isFunction(value)) {
            try {
                result = parseFunction(queryCreationContext, value)
                    .stream()
                    .map(QueryLiteral::getStringValue)
                    .map(userKeyService::getKeyForUsername)
                    .collect(Collectors.toSet());
            } catch (Exception e) {
                logger.warn("Unable to parse function", e);
            }
        } else {
            ApplicationUser user = userManager.getUserByName(value);
            if (user == null) {
                user = userManager.getUserByKey(value);
            }

            if (user != null) {
                result = ImmutableSet.of(user.getKey());
            }
        }

        return result;
    }

    private Date parseDate(QueryCreationContext queryCreationContext, String value) {
        Date result = null;

        if (isFunction(value)) {
            try {
                result = parseFunction(queryCreationContext, value)
                    .stream()
                    .map(QueryLiteral::getLongValue)
                    .filter(Objects::nonNull)
                    .map(Date::new)
                    .findAny()
                    .orElse(null);
            } catch (Exception e) {
                logger.warn("Unable to parse function", e);
            }
        } else if (jqlDateSupport.validate(value)) {
            result = jqlDateSupport.convertToDate(value);
        }

        return result;
    }

    private List<QueryLiteral> parseFunction(QueryCreationContext queryCreationContext, String functionCall) throws Exception {
        FunctionOperand functionOperand = createJqlParser(functionCall).func();
        FunctionOperandHandler operandHandler = jqlFunctionHandlerRegistry.getOperandHandler(functionOperand);

        return operandHandler.getValues(queryCreationContext, functionOperand, null);
    }

    //create JqlParser with jira class loader, since not all antlr packages are exported from jira core
    private JqlParser createJqlParser(String clauseString) throws Exception {
        ClassLoader jiraClassLoader = ClassLoaderUtil.getJiraClassLoader();

        Class<?> antlrStringStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.ANTLRStringStream");
        Class<?> commonTokenStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.CommonTokenStream");
        Class<?> charStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.CharStream");
        Class<?> tokenSourceClass = jiraClassLoader.loadClass("org.antlr.runtime.TokenSource");
        Class<?> tokenStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.TokenStream");

        Object antlrStringStream = antlrStringStreamClass.getConstructor(String.class).newInstance(clauseString);
        JqlLexer lexer = JqlLexer.class.getConstructor(charStreamClass).newInstance(antlrStringStream);

        Object commonTokenStream = commonTokenStreamClass.getConstructor(tokenSourceClass).newInstance(lexer);
        return JqlParser.class.getConstructor(tokenStreamClass).newInstance(commonTokenStream);
    }

    protected abstract Query addPermissionsCheck(QueryCreationContext queryCreationContext, Query query);

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
