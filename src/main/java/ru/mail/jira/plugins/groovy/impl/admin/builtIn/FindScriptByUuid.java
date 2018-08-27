package ru.mail.jira.plugins.groovy.impl.admin.builtIn;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.util.OnRollback;
import com.google.common.collect.ImmutableList;
import com.google.common.html.HtmlEscapers;
import com.google.common.net.UrlEscapers;
import com.opensymphony.workflow.loader.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.Union;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class FindScriptByUuid implements BuiltInScript {
    private final ApplicationProperties applicationProperties;
    private final I18nHelper i18nHelper;
    private final DatabaseAccessor databaseAccessor;
    private final WorkflowSearchService workflowSearchService;
    private final CustomFieldHelper customFieldHelper;

    @Autowired
    public FindScriptByUuid(
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
        String uuid = StringUtils.trimToNull((String) params.get("uuid"));
        if (uuid == null) {
            throw new ValidationException("UUID is required");
        }

        NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "ID");
        StringPath entityTypePath = Expressions.stringPath("ENTITY_TYPE");
        StringPath namePath = Expressions.stringPath("NAME");

        Tuple tuple = databaseAccessor.run(connection -> {
            SQLQueryFactory queryFactory = connection.query();

            Union<Tuple> union = SQLExpressions.unionAll(
                getAbstractScriptQuery(QueryDslTables.ADMIN_SCRIPT, uuid, EntityType.ADMIN_SCRIPT),
                getAbstractScriptQuery(QueryDslTables.JQL_FUNCTION, uuid, EntityType.JQL_FUNCTION),
                getAbstractScriptQuery(QueryDslTables.LISTENER, uuid, EntityType.LISTENER),
                getAbstractScriptQuery(QueryDslTables.REST, uuid, EntityType.REST),
                getAbstractScriptQuery(QueryDslTables.SCHEDULED_TASK, uuid, EntityType.SCHEDULED_TASK),
                getAbstractScriptQuery(QueryDslTables.REGISTRY_SCRIPT, uuid, EntityType.REGISTRY_DIRECTORY),
                SQLExpressions
                    .select(
                        QueryDslTables.FIELD_CONFIG.FIELD_CONFIG_ID.as(idPath),
                        Expressions.constant(""),
                        Expressions.constant(EntityType.CUSTOM_FIELD.name())
                    )
                    .from(QueryDslTables.FIELD_CONFIG)
                    .where(QueryDslTables.FIELD_CONFIG.UUID.eq(uuid))
            );

            SQLQuery<Tuple> query = queryFactory
                .select(
                    idPath,
                    namePath,
                    entityTypePath
                )
                .from(union);

            query.setUseLiterals(true);

            return query.fetchOne();
        }, OnRollback.NOOP);

        if (tuple != null) {
            EntityType type = EntityType.valueOf(tuple.get(entityTypePath));
            Integer id = tuple.get(idPath);
            String name;
            if (type == EntityType.CUSTOM_FIELD) {
                name = customFieldHelper.getFieldName((long) id);
            } else {
                name = tuple.get(namePath);
            }

            String href = applicationProperties.getString(APKeys.JIRA_BASEURL) + ScriptUtil.getPermalink(type, id);
            return "Found: <a href=\"" + href + "\"><strong>" + i18nHelper.getText(type.getI18nName()) + ":</strong> " + HtmlEscapers.htmlEscaper().escape(name) + "<a/>";
        }

        WorkflowSearchResult result = workflowSearchService.search(new WorkflowUuidSearcher(uuid)).result;

        if (result != null) {
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

            return "Found: <a href=\"" + href + "\">" +
                workflow.getName() + " - " + action.getName() + " - " + result.tab + " - " + (name != null ? name : "Unnamed") +
                "<a/>";
        }

        return "not found";

    }

    private SQLQuery<Tuple> getAbstractScriptQuery(QAbstractScript abstractScript, String uuid, EntityType entityType) {
        return SQLExpressions
            .select(
                abstractScript.ID.as("ID"),
                abstractScript.NAME.as("NAME"),
                Expressions.asString(Expressions.constantAs(entityType.name(), Expressions.stringPath("ENTITY_TYPE")))
            )
            .from(abstractScript)
            .where(abstractScript.UUID.eq(uuid));
    }

    @Override
    public String getKey() {
        return "findScriptByUuid";
    }

    @Override
    public String getI18nKey() {
        return "ru.mail.jira.plugins.groovy.adminScripts.builtIn.findScriptByUuid";
    }

    @Override
    public boolean isHtml() {
        return true;
    }

    @Override
    public List<ScriptParamDto> getParams() {
        return ImmutableList.of(
            new ScriptParamDto("uuid", "UUID", ParamType.STRING, false)
        );
    }

    private class WorkflowUuidSearcher implements WorkflowSearchCollector {
        private final String searchingForUuid;

        private JiraWorkflow workflow;
        private ActionDescriptor action;

        @Getter
        private WorkflowSearchResult result = null;

        private WorkflowUuidSearcher(String searchingForUuid) {
            this.searchingForUuid = searchingForUuid;
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
                String uuid = (String) args.get(Const.WF_UUID);

                if (searchingForUuid.equals(uuid)) {
                    result = new WorkflowSearchResult(workflow, action, "conditions", (String) args.get(Const.WF_INLINE_SCRIPT_NAME));
                }
            }
        }

        @Override
        public void collect(ValidatorDescriptor descriptor, int order) {
            Map args = descriptor.getArgs();
            String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);

            if (Const.INLINE_VALIDATOR_KEY.equals(moduleKey)) {
                String uuid = (String) args.get(Const.WF_UUID);

                if (searchingForUuid.equals(uuid)) {
                    result = new WorkflowSearchResult(workflow, action, "validators", (String) args.get(Const.WF_INLINE_SCRIPT_NAME));
                }
            }
        }

        @Override
        public void collect(FunctionDescriptor descriptor, int order) {
            Map args = descriptor.getArgs();
            String moduleKey = (String) args.get(Const.JIRA_WF_FULL_MODULE_KEY);

            if (Const.INLINE_FUNCTION_KEY.equals(moduleKey)) {
                String uuid = (String) args.get(Const.WF_UUID);

                if (searchingForUuid.equals(uuid)) {
                    result = new WorkflowSearchResult(workflow, action, "postfunctions", (String) args.get(Const.WF_INLINE_SCRIPT_NAME));
                }
            }
        }

        @Override
        public boolean isAborted() {
            return result != null;
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
