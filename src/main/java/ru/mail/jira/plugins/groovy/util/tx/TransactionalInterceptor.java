package ru.mail.jira.plugins.groovy.util.tx;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.mail.jira.plugins.groovy.util.AnnotationUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public final class TransactionalInterceptor implements MethodInterceptor {
    public static final Class<? extends Annotation> ANNOTATION_CLASS = Transactional.class;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (AnnotationUtil.isAnnotated(ANNOTATION_CLASS, invocation.getMethod())) {
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
}
