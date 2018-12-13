package ru.mail.jira.plugins.groovy.api.dto.cf;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Getter @Setter @ToString
@XmlRootElement
public class FieldConfigForm {
    @XmlElement
    private String description;
    @XmlElement
    private String scriptBody;
    @XmlElement
    private String template;
    @XmlElement
    private String comment;
    @XmlElement
    private boolean cacheable;
    @XmlElement
    private boolean velocityParamsEnabled;

    public boolean matches(FieldConfigForm other) {
        return Objects.equals(scriptBody, other.scriptBody)
            && Objects.equals(StringUtils.trimToNull(template), StringUtils.trimToNull(other.template))
            && Objects.equals(cacheable, other.cacheable)
            && Objects.equals(velocityParamsEnabled, other.velocityParamsEnabled);
    }
}
