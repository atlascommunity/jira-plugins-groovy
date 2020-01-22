package ru.mail.jira.plugins.groovy.util.tx;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.directory.ScriptDirectoryForm;
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.service.TestHelperService;
import ru.mail.jira.plugins.groovy.impl.groovy.var.GlobalObjectsBindingProvider;
import ru.mail.jira.plugins.groovy.impl.jql.ModuleManager;
import ru.mail.jira.plugins.groovy.util.cl.DelegatingClassLoader;

import java.util.List;

@Component
@ExportAsDevService(TestHelperService.class)
public class TestHelperServiceImpl implements TestHelperService {
    private final ActiveObjects activeObjects;
    private final ScriptRepository scriptRepository;
    private final DelegatingClassLoader classLoader;
    private final GlobalObjectsBindingProvider globalObjectsBindingProvider;
    private final ModuleManager moduleManager;

    @Autowired
    public TestHelperServiceImpl(
        @ComponentImport ActiveObjects activeObjects,
        ScriptRepository scriptRepository,
        DelegatingClassLoader classLoader,
        GlobalObjectsBindingProvider globalObjectsBindingProvider,
        ModuleManager moduleManager
    ) {
        this.activeObjects = activeObjects;
        this.scriptRepository = scriptRepository;
        this.classLoader = classLoader;
        this.globalObjectsBindingProvider = globalObjectsBindingProvider;
        this.moduleManager = moduleManager;
    }

    @Override
    public ActiveObjects getActiveObjects() {
        return activeObjects;
    }

    @Override
    public void transactional(ApplicationUser user, String directoryName) {
        create(user, directoryName);
    }

    @Override
    public void nonTransactional(ApplicationUser user, String directoryName) {
        create(user, directoryName);
    }

    @Override
    public <T extends Exception> T getCompilationExceptionCause(Exception e) {
        if (!(e instanceof MultipleCompilationErrorsException)) {
            throw new IllegalArgumentException("e is not compilation error exception");
        }

        MultipleCompilationErrorsException mcee = (MultipleCompilationErrorsException) e;

        List errors = mcee.getErrorCollector().getErrors();

        if (errors.size() > 1) {
            throw new IllegalArgumentException("e has more than 1 error", e);
        }

        Object error = errors.get(0);

        if (error instanceof ExceptionMessage) {
            return (T) ((ExceptionMessage) error).getCause();
        }

        throw new IllegalArgumentException("Error is not ExceptionMessage");
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }

    @Override
    public void deinitializeGlobalObjects() {
        globalObjectsBindingProvider.deinitialize();
    }

    @Override
    public void flushJqlModules() {
        moduleManager.resetDelegates();
    }

    private void create(ApplicationUser user, String directoryName) {
        ScriptDirectoryForm form = new ScriptDirectoryForm();
        form.setName(directoryName);
        form.setParentId(null);

        scriptRepository.createDirectory(user, form);

        throw new RuntimeException();
    }
}
