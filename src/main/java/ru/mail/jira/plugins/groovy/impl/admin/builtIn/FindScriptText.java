package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.util.OnRollback;
import com.google.common.collect.ImmutableList;
import com.google.common.html.HtmlEscapers;
import com.google.common.net.UrlEscapers;
import com.opensymphony.workflow.loader.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.entity.EntityType;
import ru.mail.jira.plugins.groovy.api.script.ParamType;
import ru.mail.jira.plugins.groovy.api.service.admin.BuiltInScript;
import ru.mail.jira.plugins.groovy.impl.repository.querydsl.QAbstractScript;
import ru.mail.jira.plugins.groovy.impl.workflow.search.WorkflowSearchCollector;
import ru.mail.jira.plugins.groovy.impl.workflow.search.WorkflowSearchService;
import ru.mail.jira.plugins.groovy.util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class FindScriptText implements BuiltInScript<String> {
    private final ApplicationProperties applicationProperties;
    private final I18nHelper i18nHelper;
    private final DatabaseAccessor databaseAccessor;
    private final WorkflowSearchService workflowSearchService;
    private final CustomFieldHelper customFieldHelper;

    @Autowired
    public FindScriptText(
        @ComponentImport ApplicationProperties applicationProperties,
        @ComponentImport I18nHelper i18nHelper,
        DatabaseAccessor databaseAccessor,
        WorkflowSearchService workflowSearchService,
        CustomFieldHelper customFieldHelper
    ) {
        this.applicationProperties = applicationProperties;
        this.i18nHelper = i18nHelper;
        this.databaseAccessor = databaseAccessor;
        this.workflowSearchService = workflowSearchService;
        this.customFieldHelper = customFieldHelper;
    }

    @Override
    public String run(ApplicationUser currentUser, Map<String, Object> params) throws Exception {
        String text = StringUtils.trimToNull((String) params.get("text"));
        if (text == null) {
            throw new ValidationException("Text is required");
        }
        Boolean ignoreCase = (Boolean) params.getOrDefault("ignoreCase", false);

        List<String> rows = new ArrayList<>();

        NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "ID");
        StringPath entityTypePath = Expressions.stringPath("ENTITY_TYPE");
        StringPath namePath = Expressions.stringPath("NAME");

        List<Tuple> tuples = databaseAccessor.run(connection -> {
            SQLQueryFactory queryFactory = connection.query();

            Expression<Tuple> union = SQLExpressions.unionAll(
                getAbstractScriptQuery(QueryDslTables.ADMIN_SCRIPT, text, ignoreCase, EntityType.ADMIN_SCRIPT),
                getAbstractScriptQuery(QueryDslTables.JQL_FUNCTION, text, ignoreCase, EntityType.JQL_FUNCTION),
                getAbstractScriptQuery(QueryDslTables.LISTENER, text, ignoreCase, EntityType.LISTENER),
                getAbstractScriptQuery(QueryDslTables.REST, text, ignoreCase, EntityType.REST),
                getAbstractScriptQuery(QueryDslTables.SCHEDULED_TASK, text, ignoreCase, EntityType.SCHEDULED_TASK),
                getAbstractScriptQuery(QueryDslTables.REGISTRY_SCRIPT, text, ignoreCase, EntityType.REGISTRY_SCRIPT),
                SQLExpressions
                    .select(
                        QueryDslTables.FIELD_CONFIG.FIELD_CONFIG_ID.as(idPath),
                        Expressions.constant(""),
                        Expressions.constant(EntityType.CUSTOM_FIELD.name())
                    )
                    .from(QueryDslTables.FIELD_CONFIG)
                    .where(
                        ignoreCase
                            ? QueryDslTables.FIELD_CONFIG.SCRIPT_BODY.containsIgnoreCase(text)
                            : QueryDslTables.FIELD_CONFIG.SCRIPT_BODY.contains(text)
                    )
            ).as("union");

            SQLQuery<Tuple> query = queryFactory
                .select(
                    idPath,
                    namePath,
                    entityTypePath
                )
                .from(union);

            query.setUseLiterals(true);

            return query.fetch();
        }, OnRollback.NOOP);

        for (Tuple tuple : tuples) {
            EntityType type = EntityType.valueOf(tuple.get(entityTypePath));
            Integer id = tuple.get(idPath);
            String name;
            if (type == EntityType.CUSTOM_FIELD) {
                name = customFieldHelper.getFieldName((long) id);
            } else {
                name = tuple.get(namePath);
            }

            String href = applicationProperties.getString(APKeys.JIRA_BASEURL) + ScriptUtil.getPermalink(type, id);
            rows.add(
                "<a href=\"" + href + "\"><strong>" + i18nHelper.getText(type.getI18nName()) + ":</strong> " +
                    HtmlEscapers.htmlEscaper().escape(name) +
                    "<a/>"
            );
        }

        List<WorkflowSearchResult> results = workflowSearchService.search(
            new WorkflowScriptSearcher(
                script -> ignoreCase
                    ? StringUtils.containsIgnoreCase(script, text)
                    : StringUtils.contains(script, text)
            )
        ).results;

        if (results.size() > 0) {
            results
                .stream()
                .map(result -> {
                    JiraWorkflow workflow = result.workflow;
                    ActionDescriptor action = result.action;
                    String name = StringUtils.trimToNull(result.name);
                    Collection<StepDescriptor> steps = workflow.getStepsForTransition(action);

                    String stepId = steps.size() > 0 ? String.valueOf(steps.iterator().next().getId()) : "";

                    String href = applicationProperties.getString(APKeys.JIRA_BASEURL) +
                        "/secure/admin/workflows/ViewWorkflowTransition.jspa?workflowMode=" + workflow.getMode() +
                        "&workflowName=" + UrlEscapers.urlFragmentEscaper().escape(workflow.getName()) +
                        "&descriptorTab=" + result.tab +
                        "&workflowStep=" + stepId +
                        "&workflowTransition=" + action.getId();

                    return "<a href=\"" + href + "\">" +
                        HtmlEscapers.htmlEscaper().escape(workflow.getName()) + " - " +
                        HtmlEscapers.htmlEscaper().escape(action.getName()) + " - " +
                        HtmlEscapers.htmlEscaper().escape(result.tab) + " - " +
                        (name != null ? HtmlEscapers.htmlEscaper().escape(name) : "Unnamed") +
                        "<a/>";
                })
                .forEach(rows::add);
        }

        return rows.size() > 0 ? String.join("<br/>", rows) : "not found";
    }

    private SQLQuery<Tuple> getAbstractScriptQuery(QAbstractScript script, String text, boolean ignoreCase, EntityType entityType) {
        return SQLExpressions
            .select(
                script.ID.as("ID"),
                script.NAME.as("NAME"),
                Expressions.asString(Expressions.constantAs(entityType.name(), Expressions.stringPath("ENTITY_TYPE")))
            )
            .from(script)
            .where(
                ignoreCase ? script.SCRIPT_BODY.containsIgnoreCase(text) : script.SCRIPT_BODY.contains(text),
                script.DELETED.isFalse()
            );
    }

    @Override
    public String getKey() {
        return "findScriptText";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.findScriptText";
    }

    @Override
    public boolean isHtml() {
        return true;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("text", "Text", ParamType.STRING, false),
            new ScriptParamDto("ignoreCase", "Case insensitive search", ParamType.BOOLEAN, true)
        );
    }

    private class WorkflowScriptSearcher implements WorkflowSearchCollector {
        private final Predicate<String> containsPredicate;

        private JiraWorkflow workflow;
        private ActionDescriptor action;

        @Getter
        private List<WorkflowSearchResult> results = new ArrayList<>();

        private WorkflowScriptSearcher(Predicate<String> containsPredicate) {
            this.containsPredicate = containsPredicate;
        }

        @Override
        public void setWorkflow(JiraWorkflow workflow) {
            this.workflow = workflow;
        }

        @Override
        public void workflowComplete() {

        }

        @Override
        public void setAction(ActionDescriptor action) {
            this.action = action;
        }

        @Override
        public void actionComplete() {

        }

        @Override
        public void collect(ConditionDescriptor descriptor) {
            Map args = descriptor.getArgs();
            String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);

            if (Const.INLINE_CONDITION_KEY.equals(moduleKey)) {
                String script = Base64Util.decode((String) args.get(Const.WF_INLINE_SCRIPT));

                if (script != null && containsPredicate.evaluate(script)) {
                    results.add(new WorkflowSearchResult(workflow, action, "conditions", (String) args.get(Const.WF_INLINE_SCRIPT_NAME)));
                }
            }
        }

        @Override
        public void collect(ValidatorDescriptor descriptor, int order) {
            Map args = descriptor.getArgs();
            String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);

            if (Const.INLINE_VALIDATOR_KEY.equals(moduleKey)) {
                String script = Base64Util.decode((String) args.get(Const.WF_INLINE_SCRIPT));

                if (script != null && containsPredicate.evaluate(script)) {
                    results.add(new WorkflowSearchResult(workflow, action, "validators", (String) args.get(Const.WF_INLINE_SCRIPT_NAME)));
                }
            }
        }

        @Override
        public void collect(FunctionDescriptor descriptor, int order) {
            Map args = descriptor.getArgs();
            String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);

            if (Const.INLINE_FUNCTION_KEY.equals(moduleKey)) {
                String script = Base64Util.decode((String) args.get(Const.WF_INLINE_SCRIPT));

                if (script != null && containsPredicate.evaluate(script)) {
                    results.add(new WorkflowSearchResult(workflow, action, "postfunctions", (String) args.get(Const.WF_INLINE_SCRIPT_NAME)));
                }
            }
        }
    }

    private static class WorkflowSearchResult {
        private final JiraWorkflow workflow;
        private final ActionDescriptor action;
        private final String tab;
        private final String name;

        private WorkflowSearchResult(JiraWorkflow workflow, ActionDescriptor action, String tab, String name) {
            this.workflow = workflow;
            this.action = action;
            this.tab = tab;
            this.name = name;
        }
    }
}
