package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
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
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.AbstractCommentQueryFunction;
import ru.mail.jira.plugins.groovy.util.compat.ArchivingHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public abstract class AbstractEntityQueryParser {
    private final Logger logger = LoggerFactory.getLogger(AbstractCommentQueryFunction.class);
    private final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false);

    private final ProjectRoleManager projectRoleManager;
    private final TimeZoneManager timeZoneManager;
    private final ProjectManager projectManager;
    private final JqlDateSupport jqlDateSupport;
    private final GroupManager groupManager;

    private final JqlFunctionParser jqlFunctionParser;
    private final ArchivingHelper archivingHelper;

    private final boolean isLocalDate;
    private final String createdField;
    private final String authorField;
    private final String bodyField;
    private final String levelField;
    private final String roleLevelField;

    protected AbstractEntityQueryParser(
        ProjectRoleManager projectRoleManager,
        TimeZoneManager timeZoneManager,
        ProjectManager projectManager,
        JqlDateSupport jqlDateSupport,
        GroupManager groupManager,
        JqlFunctionParser jqlFunctionParser,
        ArchivingHelper archivingHelper,
        boolean isLocalDate, String createdField, String authorField, String bodyField,
        String levelField, String roleLevelField
    ) {
        this.projectRoleManager = projectRoleManager;
        this.timeZoneManager = timeZoneManager;
        this.projectManager = projectManager;
        this.jqlDateSupport = jqlDateSupport;
        this.groupManager = groupManager;
        this.jqlFunctionParser = jqlFunctionParser;

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

        BooleanQuery.Builder query = new BooleanQuery.Builder();

        MessageSet messageSet = new MessageSetImpl();

        QueryUtil.parseQuery(messageSet, queryString).forEach((key, value) -> {
            switch (key) {
                case "by": {
                    Set<String> userKeys = jqlFunctionParser.parseUser(queryCreationContext, value);

                    if (userKeys != null && userKeys.size() > 0) {
                        if (userKeys.size() == 1) {
                            query.add(new TermQuery(new Term(authorField, userKeys.iterator().next())), BooleanClause.Occur.MUST);
                        } else {
                            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
                            for (String userKey : userKeys) {
                                booleanQuery.add(new TermQuery(new Term(authorField, userKey)), BooleanClause.Occur.SHOULD);
                            }
                            query.add(booleanQuery.build(), BooleanClause.Occur.MUST);
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
                            LongPoint.newRangeQuery(
                                createdField,
                                formatDate(since), formatDate(until)
                            ),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"on\" date is invalid");
                    }
                    break;
                }
                case "before": {
                    Date date = jqlFunctionParser.parseDate(queryCreationContext, value);

                    if (date != null) {
                        query.add(
                            LongPoint.newRangeQuery(
                                createdField,
                                Long.MIN_VALUE, formatDate(date)
                            ),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"before\" date is invalid");
                    }
                    break;
                }
                case "after": {
                    Date date = jqlFunctionParser.parseDate(queryCreationContext, value);

                    if (date != null) {
                        query.add(
                            LongPoint.newRangeQuery(
                                createdField,
                                formatDate(date), Long.MAX_VALUE
                            ),
                            BooleanClause.Occur.MUST
                        );
                    } else {
                        messageSet.addErrorMessage("\"after\" date is invalid");
                    }
                    break;
                }
                case "inRole": {
                    Collection<String> determinedProjects = queryCreationContext.getDeterminedProjects();

                    Collection<Project> projects;

                    if (determinedProjects.size() > 0) {
                        projects = projectManager.getProjectsByArgs(determinedProjects);
                    } else {
                        projects = projectManager.getProjects();
                    }

                    ProjectRole role = projectRoleManager.getProjectRole(value);

                    if (role != null) {
                        BooleanQuery.Builder projectsQuery = new BooleanQuery.Builder();
                        for (Project project : projects) {
                            if (archivingHelper.isProjectArchived(project)) {
                                logger.warn("Project {} is archived", project.getKey());
                                continue;
                            }

                            ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(role, project);

                            if (projectRoleActors != null) {
                                BooleanQuery.Builder projectQuery = new BooleanQuery.Builder();
                                projectQuery.add(
                                    new TermQuery(new Term(DocumentConstants.PROJECT_ID, String.valueOf(project.getId()))),
                                    BooleanClause.Occur.MUST
                                );

                                BooleanQuery.Builder usersQuery = new BooleanQuery.Builder();
                                for (ApplicationUser roleUser : projectRoleActors.getApplicationUsers()) {
                                    usersQuery.add(
                                        new TermQuery(new Term(authorField, roleUser.getKey())),
                                        BooleanClause.Occur.SHOULD
                                    );
                                }
                                projectQuery.add(usersQuery.build(), BooleanClause.Occur.MUST);

                                projectsQuery.add(projectQuery.build(), BooleanClause.Occur.SHOULD);
                            }
                        }
                        query.add(projectsQuery.build(), BooleanClause.Occur.MUST);
                    } else {
                        messageSet.addErrorMessage("Role \"" + value + "\" wasn't found");
                    }
                    break;
                }
                case "inGroup": {
                    Group group = groupManager.getGroup(value);

                    if (group != null) {
                        BooleanQuery.Builder groupQuery = new BooleanQuery.Builder();

                        for (ApplicationUser groupUser : groupManager.getUsersInGroup(group)) {
                            groupQuery.add(
                                new TermQuery(new Term(authorField, groupUser.getKey())),
                                BooleanClause.Occur.SHOULD
                            );
                        }
                        query.add(groupQuery.build(), BooleanClause.Occur.MUST);
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

        return new QueryParseResult(addPermissionsCheck(queryCreationContext, query.build()), messageSet);
    }

    private Long formatDate(Date date) {
        if (isLocalDate) {
            return LocalDateFactory.from(date).getEpochDays();
        } else {
            return date.getTime();
        }
    }

    protected abstract Query addPermissionsCheck(QueryCreationContext queryCreationContext, Query query);
}
