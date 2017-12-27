package ru.mail.jira.plugins.groovy.api.dto.error;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Getter @Setter
public class SyntaxError extends ScriptError {
    @XmlElement
    private int startLine;
    @XmlElement
    private int endLine;

    @XmlElement
    private int startColumn;
    @XmlElement
    private int endColumn;

    public static SyntaxError fromErrorMessage(SyntaxErrorMessage message) {
        SyntaxException cause = message.getCause();

        SyntaxError result = new SyntaxError();
        result.startLine = cause.getStartLine();
        result.endLine = cause.getEndLine();
        result.startColumn = cause.getStartColumn();
        result.endColumn = cause.getEndColumn();
        result.setMessage(cause.getMessage());

        return result;
    }
}
