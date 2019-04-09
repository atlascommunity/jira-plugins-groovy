package ru.mail.jira.plugins.groovy.api.dto.error;

import lombok.Getter;
import lombok.Setter;
import ru.mail.jira.plugins.groovy.api.script.statik.WarningMessage;

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
