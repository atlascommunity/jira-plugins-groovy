import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.WithParam

@WithParam(displayName = "Toggle", type = ParamType.BOOLEAN)
boolean isKappa

if (isKappa) {
    logger.info('Kappa')
} else {
    logger.info('Keepo')
}
