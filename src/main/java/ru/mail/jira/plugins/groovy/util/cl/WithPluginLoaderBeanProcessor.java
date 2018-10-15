package ru.mail.jira.plugins.groovy.util.cl;

import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.spring.ProxiedBeanPostProcessor;

@Component
public class WithPluginLoaderBeanProcessor extends ProxiedBeanPostProcessor {
    public WithPluginLoaderBeanProcessor() {
        super(WithPluginLoaderInterceptor.ANNOTATION_CLASS, new WithPluginLoaderInterceptor());
    }
}
