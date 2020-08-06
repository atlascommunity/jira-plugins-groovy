package ru.mail.jira.plugins.groovy.impl.groovy.log;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collections;
import java.util.List;

import static org.slf4j.event.EventConstants.NA_SUBST;

public class ContextAwareScriptLogger extends MarkerIgnoringBase implements Logger {
    private final Logger delegate;
    private final List<LoggingEvent> entries;

    public ContextAwareScriptLogger(Logger delegate, List<LoggingEvent> entries) {
        this.delegate = delegate;
        this.entries = Collections.synchronizedList(entries);
    }

    private void logEvent(Level level, String msg, Object[] params, Throwable throwable) {
        FormattingTuple ft = MessageFormatter.format(msg, params, throwable);

        LocationInfo locationInfo = new LocationInfo(NA_SUBST, NA_SUBST, NA_SUBST, "0");

        ThrowableInformation ti = null;
        Throwable t = ft.getThrowable();
        if (t != null) {
            ti = new ThrowableInformation(t);
        }

        entries.add(new LoggingEvent(
            getName(), null, System.currentTimeMillis(),
            level, ft.getMessage(),
            Thread.currentThread().getName(),
            ti, null, locationInfo, null
        ));
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        delegate.trace(msg);
        logEvent(Level.TRACE, msg, null, null);
    }

    @Override
    public void trace(String msg, Object arg) {
        delegate.trace(msg, arg);
        logEvent(Level.TRACE, msg, new Object[]{ arg }, null);
    }

    @Override
    public void trace(String msg, Object arg1, Object arg2) {
        delegate.trace(msg, arg1, arg2);
        logEvent(Level.TRACE, msg, new Object[]{ arg1, arg2 }, null);
    }

    @Override
    public void trace(String msg, Object... arguments) {
        delegate.trace(msg, arguments);
        logEvent(Level.TRACE, msg, arguments, null);
    }

    @Override
    public void trace(String msg, Throwable t) {
        delegate.trace(msg, t);
        logEvent(Level.TRACE, msg, null, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        delegate.debug(msg);
        logEvent(Level.DEBUG, msg, null, null);
    }

    @Override
    public void debug(String msg, Object arg) {
        delegate.debug(msg, arg);
        logEvent(Level.DEBUG, msg, new Object[]{arg}, null);
    }

    @Override
    public void debug(String msg, Object arg1, Object arg2) {
        delegate.debug(msg, arg1, arg2);
        logEvent(Level.DEBUG, msg, new Object[] {arg1, arg2}, null);
    }

    @Override
    public void debug(String msg, Object... arguments) {
        delegate.debug(msg, arguments);
        logEvent(Level.DEBUG, msg, arguments, null);
    }

    @Override
    public void debug(String msg, Throwable t) {
        delegate.debug(msg, t);
        logEvent(Level.DEBUG, msg, null, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        delegate.info(msg);
        logEvent(Level.INFO, msg, null, null);
    }

    @Override
    public void info(String msg, Object arg) {
        delegate.info(msg, arg);
        logEvent(Level.INFO, msg, new Object[]{arg}, null);
    }

    @Override
    public void info(String msg, Object arg1, Object arg2) {
        delegate.info(msg, arg1, arg2);
        logEvent(Level.INFO, msg, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void info(String msg, Object... arguments) {
        delegate.info(msg, arguments);
        logEvent(Level.INFO, msg, arguments, null);
    }

    @Override
    public void info(String msg, Throwable t) {
        delegate.info(msg, t);
        logEvent(Level.INFO, msg, null, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        delegate.warn(msg);
        logEvent(Level.WARN, msg, null, null);
    }

    @Override
    public void warn(String msg, Object arg) {
        delegate.warn(msg, arg);
        logEvent(Level.WARN, msg, new Object[]{arg}, null);
    }

    @Override
    public void warn(String msg, Object... arguments) {
        delegate.warn(msg, arguments);
        logEvent(Level.WARN, msg, arguments, null);
    }

    @Override
    public void warn(String msg, Object arg1, Object arg2) {
        delegate.warn(msg, arg1, arg2);
        logEvent(Level.WARN, msg, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void warn(String msg, Throwable t) {
        delegate.warn(msg, t);
        logEvent(Level.WARN, msg, null, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        delegate.error(msg);
        logEvent(Level.ERROR, msg, null, null);
    }

    @Override
    public void error(String msg, Object arg) {
        delegate.error(msg, arg);
        logEvent(Level.ERROR, msg, new Object[]{arg}, null);
    }

    @Override
    public void error(String msg, Object arg1, Object arg2) {
        delegate.error(msg, arg1, arg2);
        logEvent(Level.ERROR, msg, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void error(String msg, Object... arguments) {
        delegate.error(msg, arguments);
        logEvent(Level.ERROR, msg, arguments, null);
    }

    @Override
    public void error(String msg, Throwable t) {
        delegate.error(msg, t);
        logEvent(Level.ERROR, msg, null, t);
    }
}
