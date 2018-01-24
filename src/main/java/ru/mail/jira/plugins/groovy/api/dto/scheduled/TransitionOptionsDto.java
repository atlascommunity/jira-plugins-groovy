package ru.mail.jira.plugins.groovy.api.dto.scheduled;

import com.atlassian.jira.workflow.TransitionOptions;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter @Setter
@XmlRootElement
public class TransitionOptionsDto {
    private static final int SKIP_CONDITIONS = 0b001;
    private static final int SKIP_VALIDATORS = 0b010;
    private static final int SKIP_PERMISSIONS = 0b100;

    @XmlElement
    private boolean skipConditions = false;
    @XmlElement
    private boolean skipValidators = false;
    @XmlElement
    private boolean skipPermissions = false;

    public static TransitionOptionsDto fromInt(int value) {
        TransitionOptionsDto result = new TransitionOptionsDto();
        result.setSkipConditions((value & SKIP_CONDITIONS) == SKIP_CONDITIONS);
        result.setSkipValidators((value & SKIP_VALIDATORS) == SKIP_VALIDATORS);
        result.setSkipPermissions((value & SKIP_PERMISSIONS) == SKIP_PERMISSIONS);
        return result;
    }

    public int toInt() {
        return (skipConditions ? SKIP_CONDITIONS : 0) | (skipValidators ? SKIP_VALIDATORS : 0) | (skipPermissions ? SKIP_PERMISSIONS : 0);
    }

    public TransitionOptions toJiraOptions() {
        TransitionOptions.Builder builder = new TransitionOptions.Builder();

        if (skipConditions) {
            builder.skipConditions();
        }
        if (skipValidators) {
            builder.skipValidators();
        }
        if (skipPermissions) {
            builder.skipPermissions();
        }

        return builder.build();
    }
}
