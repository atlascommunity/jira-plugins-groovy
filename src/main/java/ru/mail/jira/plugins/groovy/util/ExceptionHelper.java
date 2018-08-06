package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import ru.mail.jira.plugins.groovy.api.dto.error.CompilationMessage;
import ru.mail.jira.plugins.groovy.api.dto.error.PositionedCompilationMessage;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.WarningMessage;

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
            "field", fieldName,
            "error", e
                .getErrorCollector()
                .getErrors()
                .stream()
                .map(msg -> mapCompilationMessage("error", msg))
                .collect(Collectors.toList())
        );
    }

    public static Object mapCompilationMessage(String type, Object message) {
        if (message instanceof SyntaxErrorMessage) {
            return PositionedCompilationMessage.fromErrorMessage(type, (SyntaxErrorMessage) message);
        }
        if (message instanceof ExceptionMessage) {
            return new CompilationMessage(type, ((ExceptionMessage) message).getCause().getMessage());
        }
        if (message instanceof WarningMessage) {
            return PositionedCompilationMessage.fromWarning((WarningMessage) message);
        }
        return null;
    }

    public static String getMessageOrClassName(Exception e) {
        if (e.getMessage() != null) {
            return e.getMessage();
        }
        return e.getClass().getSimpleName();
    }
}
