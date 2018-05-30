package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.dto.cf.PreviewForm;
import ru.mail.jira.plugins.groovy.api.dto.cf.PreviewResult;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class FieldPreviewService {
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final FieldConfigManager fieldConfigManager;
    private final IssueManager issueManager;
    private final TemplateRenderer templateRenderer;
    private final FieldLayoutManager fieldLayoutManager;
    private final FieldValueExtractor fieldValueExtractor;

    @Autowired
    public FieldPreviewService(
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport VelocityRequestContextFactory velocityRequestContextFactory,
        @ComponentImport FieldConfigManager fieldConfigManager,
        @ComponentImport IssueManager issueManager,
        @ComponentImport TemplateRenderer templateRenderer,
        @ComponentImport FieldLayoutManager fieldLayoutManager,
        FieldValueExtractor fieldValueExtractor
    ) {
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.fieldConfigManager = fieldConfigManager;
        this.issueManager = issueManager;
        this.templateRenderer = templateRenderer;
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldValueExtractor = fieldValueExtractor;
    }

    public PreviewResult preview(ApplicationUser user, long configId, PreviewForm form) throws IOException {
        String issueKey = form.getIssueKey();
        MutableIssue issue = issueManager.getIssueByCurrentKey(issueKey);
        if (issue == null) {
            throw new ValidationException("Issue was not found");
        }

        FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(configId);
        if (fieldConfig == null) {
            throw new ValidationException("Unknown field config");
        }

        FieldConfigForm configForm = form.getConfigForm();

        CustomField customField = fieldConfig.getCustomField();
        if (!(customField.getCustomFieldType() instanceof ScriptedCFType)) {
            throw new ValidationException("Invalid custom field type");
        }
        ScriptedCFType type = ((ScriptedCFType) customField.getCustomFieldType());

        FieldScript script = new FieldScript();
        script.setCacheable(false);
        script.setId(UUID.randomUUID().toString());
        script.setScriptBody(configForm.getScriptBody());
        script.setTemplate(configForm.getTemplate());
        script.setWithVelocityParams(configForm.isVelocityParamsEnabled());

        ValueHolder result = fieldValueExtractor.preview(
            issue,
            customField,
            script
        );

        long t = System.currentTimeMillis();

        Map<String, Object> velocityParams = new HashMap<>();
        velocityParams.putAll(
            CustomFieldUtils.buildParams(
                customField,
                fieldConfig,
                issue,
                fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(customField),
                result.getValue(),
                null, null, null
            )
        );

        type.fillStaticVelocityParams(velocityParams);

        velocityParams.put("value", result.getValue());
        velocityParams.put("template", configForm.getTemplate());
        if (result.getVelocityParams() != null) {
            velocityParams.putAll(result.getVelocityParams());
        }

        String template = customField
            .getCustomFieldType()
            .getDescriptor()
            .getResourceDescriptor("velocity", "view")
            .getLocation();

        StringWriter stringWriter = new StringWriter();
        templateRenderer.render(template, collectParams(velocityParams), stringWriter);

        return new PreviewResult(stringWriter.toString(), System.currentTimeMillis() - t);
    }

    private Map<String, Object> collectParams(Map<String, Object> startingParams) {
        Map<String, Object> params = velocityRequestContextFactory.getDefaultVelocityParams(startingParams, this.authenticationContext);
        Map<String, Object> result = new HashMap<>();
        if (!params.containsKey("i18n")) {
            result.put("i18n", authenticationContext.getI18nHelper());
        }

        if (!params.containsKey("ctx")) {
            result.put("ctx", params);
        }

        result.put("descriptor", this);
        return CompositeMap.of(result, params);
    }
}
