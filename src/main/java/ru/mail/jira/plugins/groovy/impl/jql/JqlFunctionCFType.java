package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nullable;

public class JqlFunctionCFType extends CalculatedCFType<Void, Void> {
    @Override
    public String getStringFromSingularObject(Void aVoid) {
        return null;
    }

    @Override
    public Void getSingularObjectFromString(String s) throws FieldValidationException {
        return null;
    }

    @Nullable
    @Override
    public Void getValueFromIssue(CustomField customField, Issue issue) {
        return null;
    }
}
