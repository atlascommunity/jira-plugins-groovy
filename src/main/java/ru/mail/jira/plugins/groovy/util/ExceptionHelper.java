package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import ru.mail.jira.plugins.groovy.api.dto.error.CompilationMessage;
import ru.mail.jira.plugins.groovy.api.dto.error.PositionedCompilationMessage;
import ru.mail.jira.plugins.groovy.api.script.statik.WarningMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExceptionHelper {
    private ExceptionHelper() {
    }

    public static String writeExceptionToString(Throwable e) {
        if (e == null) {
            return null;
        }
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
            return buildCompilationMessage(type, (SyntaxErrorMessage) message);
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

    private static PositionedCompilationMessage buildCompilationMessage(String type, SyntaxErrorMessage message) {
        SyntaxException cause = message.getCause();

        PositionedCompilationMessage result = new PositionedCompilationMessage();
        result.setStartLine(cause.getStartLine());
        result.setEndLine(cause.getEndLine());
        result.setStartColumn(cause.getStartColumn());
        result.setEndColumn(cause.getEndColumn());
        result.setMessage(cause.getMessage());
        result.setType(type);

        return result;
    }
}
