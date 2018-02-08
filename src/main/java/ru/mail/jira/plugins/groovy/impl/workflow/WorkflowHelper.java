package ru.mail.jira.plugins.groovy.impl.workflow;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.workflow.WorkflowScriptType;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptDto;
import ru.mail.jira.plugins.groovy.api.dto.ScriptParamDto;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.ScriptParamFactory;
import ru.mail.jira.plugins.groovy.util.Base64Util;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class WorkflowHelper {
    private final Logger logger = LoggerFactory.getLogger(WorkflowHelper.class);

    private final ScriptService scriptService;
    private final ScriptRepository scriptRepository;
    private final ExecutionRepository executionRepository;
    private final ScriptParamFactory scriptParamFactory;

    @Autowired
    public WorkflowHelper(
        ScriptService scriptService,
        ScriptRepository scriptRepository,
        ExecutionRepository executionRepository,
        ScriptParamFactory scriptParamFactory
    ) {
        this.scriptService = scriptService;
        this.scriptRepository = scriptRepository;
        this.executionRepository = executionRepository;
        this.scriptParamFactory = scriptParamFactory;
    }

    public ScriptDescriptor getScript(Map args, WorkflowScriptType type) {
        Map<String, Object> paramBindings = ImmutableMap.of();
        String scriptString = Base64Util.decode((String) args.get(Const.WF_INLINE_SCRIPT));
        String id = (String) args.get(Const.WF_UUID);
        boolean fromRegistry = false;

        if (scriptString == null) {
            String scriptIdString = (String) args.get(Const.WF_REPOSITORY_SCRIPT_ID);
            if (scriptIdString != null) {
                Integer scriptId = Ints.tryParse(scriptIdString);
                if (scriptId != null) {
                    RegistryScriptDto script = scriptRepository.getScript(scriptId, false, false);
                    if (script != null) {
                        id = String.valueOf(script.getId());
                        scriptString = script.getScriptBody();
                        fromRegistry = true;

                        if (!script.getTypes().contains(type)) {
                            logger.warn("Script {}({}) is executed with unsupported type: {}", script.getName(), script.getId(), type);
                        }

                        if (script.getParams() != null) {
                            paramBindings = new HashMap<>();

                            for (ScriptParamDto param : script.getParams()) {
                                String paramName = param.getName();
                                String value = Base64Util.decode((String) args.get(Const.getParamKey(paramName)));

                                if (value == null) {
                                    logger.error("Value for script param {} is not found", paramName);
                                    return null;
                                } else {
                                    paramBindings.put(paramName, scriptParamFactory.getParamObject(param, value));
                                }
                            }
                        }

                        if (script.isDeleted()) {
                            logger.warn("Deleted script is used {}", id); //todo: log workflow & action
                        }
                    } else {
                        logger.error("unable to find script with id {}", scriptId);
                    }
                } else {
                    logger.error("id is not a number {}", scriptIdString);
                    return null;
                }
            }
        }
        scriptString = StringUtils.trimToNull(scriptString);

        if (scriptString == null || id == null) {
            return null;
        }

        return new ScriptDescriptor(id, fromRegistry, scriptString, paramBindings);
    }

    public Object executeScript(ScriptDescriptor script, ScriptType type, Issue issue, ApplicationUser user, Map transientVars) throws WorkflowException {
        Object result = null;
        WorkflowException rethrow = null;

        boolean success = true;
        String error = null;
        String id = script.getId();

        HashMap<String, Object> bindings = new HashMap<>(script.getParams());
        bindings.put("issue", issue);
        bindings.put("currentUser", user);
        bindings.put("transientVars", transientVars);

        long t = System.currentTimeMillis();
        try {
            result = scriptService.executeScript(
                id,
                script.getScriptBody(),
                type,
                bindings
            );
        } catch (WorkflowException e) {
            rethrow = e;
        } catch (Exception e) {
            success = false;
            error = ExceptionHelper.writeExceptionToString(e);
            logger.error("Exception occurred while executing script {} for issue {}", id, issue.getKey(), e);
        } finally {
            t = System.currentTimeMillis() - t;
        }

        ImmutableMap<String, String> params = ImmutableMap.of(
            "issue", Objects.toString(issue, ""),
            "currentUser", Objects.toString(user, ""),
            "transientVars", Objects.toString(transientVars, ""),
            "type", type.name(),
            "params", script.getParams().toString()
        );
        if (script.isFromRegistry()) {
            Integer parsedId = Ints.tryParse(id);

            if (parsedId != null) {
                executionRepository.trackFromRegistry(parsedId, t, success, error, params);
            }
        } else {
            executionRepository.trackInline(id, t, success, error, params);
        }

        if (rethrow != null) {
            throw rethrow;
        }

        return result;
    }
}
