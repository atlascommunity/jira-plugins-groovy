package ru.mail.jira.plugins.groovy.api.dao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldConfigForm;
import ru.mail.jira.plugins.groovy.api.entity.FieldScript;

public interface FieldConfigDao {
    FieldScript findByConfigId(long id);

    @Transactional
    FieldScript createConfig(ApplicationUser user, FieldConfig jiraFieldConfig, FieldConfigForm form);

    @Transactional
    FieldScript updateConfig(ApplicationUser user, int id, FieldConfig jiraFieldConfig, FieldConfigForm form);
}
