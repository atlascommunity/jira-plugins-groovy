package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Usage example:  groovyFunction in my_memberOfRole("assignee", "Administrators")
 *
 * Function supports 4 fields: "watchers", "assignee", "author", "creator"
 *
 * Can be used with multiple roles as multiple arguments:
 *
 * groovyFunction in my_memberOfRole("assignee", "Administrators", "Developers", "Users")
 */
@Component
public class MemberOfRoleFunction extends AbstractBuiltInQueryFunction {

  private final static Map<String, String> fields = ImmutableMap.of(
      "watchers", DocumentConstants.ISSUE_WATCHERS,
      "assignee", DocumentConstants.ISSUE_ASSIGNEE,
      "author", DocumentConstants.ISSUE_AUTHOR,
      "creator", DocumentConstants.ISSUE_CREATOR);

  private final ProjectManager projectManager;
  private final ProjectRoleManager projectRoleManager;

  @Autowired
  public MemberOfRoleFunction(
      @ComponentImport ProjectManager projectManager,
      @ComponentImport ProjectRoleManager projectRoleManager) {
    super("memberOfRole", 2);
    this.projectManager = projectManager;
    this.projectRoleManager = projectRoleManager;
  }

  @Nonnull
  @Override
  public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
    return ImmutableList.of();
  }

  @Override
  protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
    FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();

    String field = functionOperand.getArgs().get(0);

    if (!fields.containsKey(field.toLowerCase(Locale.ROOT))) {
      messageSet.addErrorMessage(String.format("Field \"%s\" is not supported", field));
      return;
    }
    if (operand.getArgs().size() < 2) {
      messageSet.addErrorMessage("Two args minimal required. Example of use  \"my_memberOfRole(\"Assignee\", \"Administrators\")\"");
      return;
    }
    if (getProjectRoles(terminalClause).isEmpty()) {
      messageSet.addErrorMessage("Provided roles not exists.");
    }
  }

  @Nonnull
  @Override
  public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
    Set<ProjectRole> roles = getProjectRoles(terminalClause);
    Set<String> projects = queryCreationContext.getDeterminedProjects();
    FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();
    String field = operand.getArgs().get(0);

    BooleanQuery.Builder builder = new BooleanQuery.Builder();

    for (ProjectRole role : roles) {
      if (!projects.isEmpty()) {
        for (String key : projects) {
          Project project = projectManager.getProjectObjByKey(key);
          handleFields(builder, field, projectRoleManager.getProjectRoleActors(role, project).getApplicationUsers());
        }
      } else {
        handleFields(builder, field, projectRoleManager.getDefaultRoleActors(role).getApplicationUsers());
      }
    }

    return new QueryFactoryResult(builder.build(), terminalClause.getOperator().equals(Operator.NOT_IN));
  }

  private void handleFields(BooleanQuery.Builder builder, String field, Set<ApplicationUser> users) {
    Optional<String> selectedField = fields.keySet().stream().filter(f -> f.equals(field.toLowerCase(Locale.ROOT))).findFirst();
    if (!selectedField.isPresent()) return;

    for (ApplicationUser user : users) {
      builder.add(new TermQuery(new Term(fields.get(selectedField.get()), user.getKey())), BooleanClause.Occur.MUST);
    }
  }

  private Set<ProjectRole> getProjectRoles(TerminalClause terminalClause) {
    FunctionOperand operand = (FunctionOperand) terminalClause.getOperand();

    List<String> args = operand.getArgs();

    List<String> roleArgs = args.subList(1, args.size()).stream().map(String::toLowerCase).collect(Collectors.toList());

    return new HashSet<>(projectRoleManager.getProjectRoles())
        .stream()
        .filter(projectRole -> roleArgs.contains(projectRole.getName().toLowerCase(Locale.ROOT)))
        .collect(Collectors.toSet());
  }
}
