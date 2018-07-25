package ru.mail.jira.plugins.groovy.util;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.Optional;

public final class FieldUtil {
    private FieldUtil() {}

    public static Optional<String> getSearcherKey(CustomField field) {
        CustomFieldSearcher searcher = field.getCustomFieldSearcher();
        if (searcher != null) {
            return Optional.of(searcher.getDescriptor().getCompleteKey());
        }

        return Optional.empty();
    }
}
