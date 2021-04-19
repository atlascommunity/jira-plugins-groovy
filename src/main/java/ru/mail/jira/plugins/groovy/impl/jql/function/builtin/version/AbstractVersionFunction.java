package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.version;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.JqlFunctionParser;
import ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query.QueryUtil;

import javax.annotation.Nonnull;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractVersionFunction implements CustomFunction {
    private final VersionManager versionManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JqlFunctionParser jqlFunctionParser;
    private final Function<Version, Date> dateExtractor;
    private final String functionName;

    protected AbstractVersionFunction(
        VersionManager versionManager,
        ProjectManager projectManager,
        PermissionManager permissionManager,
        JqlFunctionParser jqlFunctionParser,
        Function<Version, Date> dateExtractor,
        String functionName
    ) {
        this.versionManager = versionManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.jqlFunctionParser = jqlFunctionParser;
        this.dateExtractor = dateExtractor;
        this.functionName = "my_" + functionName;
    }

    @Override
    public String getModuleKey() {
        return "jql-function-builtin-" + functionName;
    }

    @Override
    public void init(@Nonnull JqlFunctionModuleDescriptor jqlFunctionModuleDescriptor) { }

    @Nonnull
    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Nonnull
    @Override
    public final MessageSet validate(
        ApplicationUser applicationUser,
        @Nonnull FunctionOperand functionOperand,
        @Nonnull TerminalClause terminalClause
    ) {
        MessageSetImpl messageSet = new MessageSetImpl();
        parseQuery(new QueryCreationContextImpl(applicationUser), functionOperand.getArgs().get(0), messageSet);
        return messageSet;
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(
        @Nonnull QueryCreationContext queryCreationContext,
        @Nonnull FunctionOperand functionOperand,
        @Nonnull TerminalClause terminalClause
    ) {
        MessageSetImpl messageSet = new MessageSetImpl();
        Predicate<Version> predicate = parseQuery(queryCreationContext, functionOperand.getArgs().get(0), messageSet);

        Set<String> determinedProjects = queryCreationContext.getDeterminedProjects();

        Collection<Project> projects = (
            determinedProjects.size() > 0
                ? projectManager.getProjectsByArgs(determinedProjects)
                : projectManager.getProjects()
            )
            .stream()
            .filter(project ->
                permissionManager.hasPermission(
                    ProjectPermissions.BROWSE_PROJECTS,
                    project,
                    queryCreationContext.getApplicationUser()
                )
            )
            .collect(Collectors.toList());

        Collection<Version> versions = versionManager.getAllVersionsForProjects(projects, true);

        return versions
            .stream()
            .filter(predicate)
            .map(version -> new QueryLiteral(functionOperand, version.getId()))
            .collect(Collectors.toList());
    }

    @Override
    public final boolean isList() {
        return true;
    }

    @Override
    public final int getMinimumNumberOfExpectedArguments() {
        return 1;
    }

    @Nonnull
    @Override
    public final JiraDataType getDataType() {
        return JiraDataTypes.VERSION;
    }

    private Predicate<Version> parseQuery(QueryCreationContext queryCreationContext, String query, MessageSet messageSet) {
        List<Predicate<Date>> predicates = new ArrayList<>();

        QueryUtil
            .parseQuery(messageSet, query)
            .forEach((field, value) -> {
                Date tempDate = jqlFunctionParser.parseDate(queryCreationContext, value);
                if (tempDate != null) {
                    tempDate = Date.from(
                        tempDate
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                    );
                } else {
                    messageSet.addErrorMessage("\"" + field + "\" date is invalid");
                    return;
                }

                Date date = tempDate;

                if ("on".equals(field)) {
                    predicates.add(versionDate -> versionDate.getTime() == date.getTime());
                } else if ("after".equals(field)) {
                    predicates.add(versionDate -> versionDate.after(date));
                } else if ("before".equals(field)) {
                    predicates.add(versionDate -> versionDate.before(date));
                } else {
                    messageSet.addErrorMessage("Invalid field \"" + field + "\"");
                }
            });

        if (predicates.size() == 0) {
            messageSet.addErrorMessage("Invalid query");
            return version -> false;
        }

        return version -> {
            Date date = dateExtractor.apply(version);
            return date != null && predicates.stream().allMatch(predicate -> predicate.test(date));
        };
    }
}
