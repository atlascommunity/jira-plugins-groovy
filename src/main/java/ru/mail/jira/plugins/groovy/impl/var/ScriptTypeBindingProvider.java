package ru.mail.jira.plugins.groovy.impl.var;

import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;

import javax.annotation.Nonnull;

public class ScriptTypeBindingProvider implements BindingDescriptor<ScriptType> {
    @Override
    public ScriptType getValue(ScriptType type, String scriptId) {
        return type;
    }

    @Nonnull
    @Override
    public Class<ScriptType> getType() {
        return ScriptType.class;
    }

    @Nonnull
    @Override
    public ClassDoc getDoc() {
        return new ClassDoc(
            true, getType().getCanonicalName(),
            "https://github.com/atlascommunity/jira-plugins-groovy/blob/master/src/main/java/ru/mail/jira/plugins/groovy/api/script/ScriptType.java"
        );
    }

    @Override
    public void dispose() {}
}
