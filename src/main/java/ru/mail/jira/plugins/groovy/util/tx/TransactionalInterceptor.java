package ru.mail.jira.plugins.groovy.util.tx;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TransactionalInterceptor implements MethodInterceptor {
    private static final Class<? extends Annotation> ANNOTATION_CLASS = Transactional.class;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (isAnnotated(invocation.getMethod())) {
            return executeInTransaction(invocation);
        } else {
            return invocation.proceed();
        }
    }

    private Object executeInTransaction(MethodInvocation invocation) throws Throwable {
        Transaction transaction = Txn.begin();
        try {
            Object result = invocation.proceed();

            transaction.commit();

            return result;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            transaction.finallyRollbackIfNotCommitted();
        }
    }

    static boolean isAnnotated(Method method) {
        return method != null && (isAnnotationPresent(method) || isAnnotationPresent(method.getDeclaringClass()));
    }

    public static boolean isAnnotated(Class c) {
        if (c != null) {
            if (c.isInterface()) {
                if (isAnnotationPresent(c)) {
                    return true;
                }
                for (Method method : c.getMethods()) {
                    if (isAnnotated(method)) {
                        return true;
                    }
                }
            } else {
                if (isAnnotationPresent(c)) {
                    throw new RuntimeException("Transactional is not supported for concrete classes");
                }
                for (Method method : c.getMethods()) {
                    if (isAnnotated(method)) {
                        throw new RuntimeException("Transactional is not supported for concrete classes");
                    }
                }
            }

            for (Class ifce : c.getInterfaces()) {
                if (isAnnotated(ifce)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAnnotationPresent(AnnotatedElement e) {
        return e.isAnnotationPresent(ANNOTATION_CLASS);
    }
}
