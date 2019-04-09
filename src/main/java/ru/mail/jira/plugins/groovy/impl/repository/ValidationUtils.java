package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.groovy.api.dto.ScriptForm;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.RestFieldException;
import ru.mail.jira.plugins.groovy.util.ValidationException;

final class ValidationUtils {
    private ValidationUtils() {};

    @Deprecated
    static void validateForm(I18nHelper i18nHelper, boolean isNew, ScriptForm form) {
        form.setName(StringUtils.trimToNull(form.getName()));

        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (form.getName().length() > 64) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "name");
        }

        String description = StringUtils.trimToNull(form.getDescription());
        form.setDescription(description);
        if (description != null) {
            if (form.getDescription().length() > 10000) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "description");
            }
        }

        String comment = StringUtils.trimToNull(form.getComment());
        form.setComment(comment);

        if (!isNew) {
            if (StringUtils.isEmpty(comment)) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }
        }

        if (comment != null) {
            if (comment.length() > Const.COMMENT_MAX_LENGTH) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "comment");
            }
        }
    }

    static void validateForm2(I18nHelper i18nHelper, boolean isNew, ScriptForm form) {
        if (StringUtils.isEmpty(form.getName())) {
            throw new ValidationException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (form.getName().length() > 64) {
            throw new ValidationException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "name");
        }

        String description = StringUtils.trimToNull(form.getDescription());
        form.setDescription(description);
        if (description != null) {
            if (form.getDescription().length() > 10000) {
                throw new ValidationException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "description");
            }
        }

        String comment = StringUtils.trimToNull(form.getComment());
        form.setComment(comment);

        if (!isNew) {
            if (StringUtils.isEmpty(comment)) {
                throw new ValidationException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }
        }

        if (comment != null) {
            if (comment.length() > Const.COMMENT_MAX_LENGTH) {
                throw new ValidationException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.valueTooLong"), "comment");
            }
        }
    }
}
