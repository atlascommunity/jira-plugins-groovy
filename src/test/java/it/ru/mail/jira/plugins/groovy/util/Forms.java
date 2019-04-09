package it.ru.mail.jira.plugins.groovy.util;

import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.impl.FileUtil;

import java.io.IOException;

public final class Forms {
    private Forms() {}

    public static GlobalObjectForm globalObject(String sourcePath) throws IOException {
        return globalObjectFromSource(FileUtil.readArquillianExample(sourcePath));
    }

    public static GlobalObjectForm globalObjectFromSource(String source) {
        long ts = System.currentTimeMillis();
        String globalObjectName = "testObject" + ts;

        GlobalObjectForm form = new GlobalObjectForm();
        form.setName(globalObjectName);
        form.setScriptBody(source);

        return form;
    }
}
