package ru.mail.jira.plugins.groovy.impl.groovy;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Getter @Setter
public class ScriptInjection {
    private String plugin;
    private String className;
    private String variableName;

    public ScriptInjection(String plugin, String className, String variableName) {
        this.plugin = plugin;
        this.className = className;
        this.variableName = variableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptInjection that = (ScriptInjection) o;
        return Objects.equals(plugin, that.plugin) &&
            Objects.equals(className, that.className) &&
            Objects.equals(variableName, that.variableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, className, variableName);
    }
}
