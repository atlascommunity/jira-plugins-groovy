package ru.mail.jira.plugins.groovy.util.tx;

import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.spring.ProxiedBeanPostProcessor;

@Component
public final class TransactionalAnnotationProcessor extends ProxiedBeanPostProcessor {
    public TransactionalAnnotationProcessor() {
        super(TransactionalInterceptor.ANNOTATION_CLASS, new TransactionalInterceptor());
    }
}
