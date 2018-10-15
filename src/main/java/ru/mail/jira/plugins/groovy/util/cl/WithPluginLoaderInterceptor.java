package ru.mail.jira.plugins.groovy.util.cl;

import com.atlassian.plugin.util.ClassLoaderStack;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.mail.jira.plugins.groovy.util.AnnotationUtil;

import java.lang.annotation.Annotation;

public class WithPluginLoaderInterceptor implements MethodInterceptor {
    public static final Class<? extends Annotation> ANNOTATION_CLASS = WithPluginLoader.class;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (AnnotationUtil.isAnnotated(ANNOTATION_CLASS, invocation.getMethod())) {
            ClassLoaderStack.push(ClassLoaderUtil.getCurrentPluginClassLoader());
            try {
                return invocation.proceed();
            } finally {
                ClassLoaderStack.pop();
            }
        } else {
            return invocation.proceed();
        }
    }
}
