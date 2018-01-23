package ru.mail.jira.plugins.groovy.impl.scheduled;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.springframework.stereotype.Component;

@ExportAsService(LifecycleAware.class)
@Component
public class ScheduledTaskService implements LifecycleAware {
    @Override
    public void onStart() {
        
    }

    @Override
    public void onStop() {

    }
}
