package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import ru.mail.jira.plugins.groovy.api.dto.error.ScriptError;
import ru.mail.jira.plugins.groovy.api.dto.error.SyntaxError;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExceptionHelper {
    private ExceptionHelper() {
    }

    public static String writeExceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return e.getMessage() + "\r\n" + sw.toString();
    }

    public static Map<String, Object> mapCompilationException(String fieldName, MultipleCompilationErrorsException e) {
        return ImmutableMap.of(
            "field", "scriptBody",
            "error", e
                .getErrorCollector()
                .getErrors()
                .stream()
                .map(ExceptionHelper::mapMessage)
                .collect(Collectors.toList())
        );
    }

    private static Object mapMessage(Object message) {
        if (message instanceof SyntaxErrorMessage) {
            return SyntaxError.fromErrorMessage((SyntaxErrorMessage) message);
        }
        if (message instanceof ExceptionMessage) {
            return new ScriptError(((ExceptionMessage) message).getCause().getMessage());
        }
        return null;
    }
}
