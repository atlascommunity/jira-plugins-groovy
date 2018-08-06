package ru.mail.jira.plugins.groovy.api.dto.error;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.WarningMessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Getter @Setter
public class PositionedCompilationMessage extends CompilationMessage {
    @XmlElement
    private int startLine;
    @XmlElement
    private int endLine;

    @XmlElement
    private int startColumn;
    @XmlElement
    private int endColumn;

    public static PositionedCompilationMessage fromErrorMessage(String type, SyntaxErrorMessage message) {
        SyntaxException cause = message.getCause();

        PositionedCompilationMessage result = new PositionedCompilationMessage();
        result.startLine = cause.getStartLine();
        result.endLine = cause.getEndLine();
        result.startColumn = cause.getStartColumn();
        result.endColumn = cause.getEndColumn();
        result.setMessage(cause.getMessage());
        result.setType(type);

        return result;
    }

    public static PositionedCompilationMessage fromWarning(WarningMessage message) {
        PositionedCompilationMessage result = new PositionedCompilationMessage();
        result.startLine = message.getStartLine();
        result.endLine = message.getEndLine();
        result.startColumn = message.getStartColumn();
        result.endColumn = message.getEndColumn();
        result.setMessage(message.getMessage());
        result.setType("warning");

        return result;
    }
}
