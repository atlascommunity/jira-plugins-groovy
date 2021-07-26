package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionForm;
import ru.mail.jira.plugins.groovy.api.dto.jql.JqlFunctionScriptDto;
import ru.mail.jira.plugins.groovy.api.repository.JqlFunctionRepository;
import ru.mail.jira.plugins.groovy.api.service.JqlFunctionService;
import ru.mail.jira.plugins.groovy.api.jql.CustomFunction;
import ru.mail.jira.plugins.groovy.api.util.PluginLifecycleAware;

import java.util.*;

@Component
@ExportAsDevService
public class JqlFunctionServiceImpl implements JqlFunctionService, PluginLifecycleAware {
    private static final String LOCK_KEY = "ru.mail.jira.groovy.jqlFunction";

    private final ClusterLockService lockService;
    private final ModuleManager moduleManager;
    private final ModuleReplicationService replicationService;
    private final JqlFunctionRepository repository;
    private final List<CustomFunction> builtInFunctions;

    @Autowired
    public JqlFunctionServiceImpl(
        @ComponentImport ClusterLockService lockService,
        ModuleManager moduleManager,
        ModuleReplicationService replicationService,
        JqlFunctionRepository repository,
        Optional<List<CustomFunction>> builtInFunctions
    ) {
        this.lockService = lockService;
        this.moduleManager = moduleManager;
        this.replicationService = replicationService;
        this.repository = repository;
        this.builtInFunctions = builtInFunctions.orElse(ImmutableList.of());
    }

    @Override
    public void onStart() {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            for (CustomFunction function : builtInFunctions) {
                moduleManager.registerBuiltInFunction(function);
            }

            for (JqlFunctionScriptDto script : repository.getAllScripts(true, false, false)) {
                moduleManager.registerScript(script);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onStop() {
        moduleManager.unregisterAll();
    }

    @Override
    public int getInitOrder() {
        return 101;
    }

    @Override
    public JqlFunctionScriptDto createScript(ApplicationUser user, JqlFunctionForm form) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            JqlFunctionScriptDto script = repository.createScript(user, form);
            moduleManager.registerScript(script);
            replicationService.registerModule(script.getId());
            return script;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public JqlFunctionScriptDto updateScript(ApplicationUser user, int id, JqlFunctionForm form) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            JqlFunctionScriptDto script = repository.updateScript(user, id, form);
            moduleManager.registerScript(script);
            replicationService.registerModule(script.getId());
            return script;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteScript(ApplicationUser user, int id) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            repository.deleteScript(user, id);
            moduleManager.unregisterScript(id);
            replicationService.unregisterModule(id);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void restoreScript(ApplicationUser user, int id) {
        ClusterLock lock = lockService.getLockForName(LOCK_KEY);

        lock.lock();
        try {
            repository.restoreScript(user, id);
            moduleManager.registerScript(repository.getScript(id));
            replicationService.unregisterModule(id);
        } finally {
            lock.unlock();
        }
    }
}
