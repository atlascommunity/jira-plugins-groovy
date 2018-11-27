package ru.mail.jira.plugins.groovy.util.compat;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArchivingHelperFactory implements FactoryBean<ArchivingHelper> {
    private BuildUtilsInfo buildUtilsInfo;

    @Autowired
    public void setBuildUtilsInfo(
        @ComponentImport BuildUtilsInfo buildUtilsInfo
    ) {
        this.buildUtilsInfo = buildUtilsInfo;
    }

    @Override
    public ArchivingHelper getObject() {
        int[] versionNumbers = buildUtilsInfo.getVersionNumbers();

        if (versionNumbers[0] == 7 && versionNumbers[1] >= 10 || versionNumbers[0] > 7) {
            return new ArchivingHelperImpl();
        } else {
            return new StubArchivingHelper();
        }
    }

    @Override
    public Class<?> getObjectType() {
        return ArchivingHelper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
