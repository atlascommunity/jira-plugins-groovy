package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dao.GlobalObjectDao;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectDto;
import ru.mail.jira.plugins.groovy.api.dto.global.GlobalObjectForm;
import ru.mail.jira.plugins.groovy.api.entity.GlobalObject;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.GlobalObjectRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;
import ru.mail.jira.plugins.groovy.util.ChangelogHelper;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.RestFieldException;
import ru.mail.jira.plugins.groovy.util.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ExportAsDevService(GlobalObjectRepository.class)
public class GlobalObjectRepositoryImpl implements GlobalObjectRepository {
    private final I18nHelper i18nHelper;
    private final GlobalObjectDao globalObjectDao;
    private final ScriptService scriptService;
    private final ScriptInvalidationService invalidationService;
    private final ChangelogHelper changelogHelper;
    private final ExecutionRepository executionRepository;

    @Autowired
    public GlobalObjectRepositoryImpl(
        @ComponentImport I18nHelper i18nHelper,
        GlobalObjectDao globalObjectDao,
        ScriptService scriptService,
        ScriptInvalidationService invalidationService,
        ChangelogHelper changelogHelper,
        ExecutionRepository executionRepository
    ) {
        this.i18nHelper = i18nHelper;
        this.globalObjectDao = globalObjectDao;
        this.scriptService = scriptService;
        this.invalidationService = invalidationService;
        this.changelogHelper = changelogHelper;
        this.executionRepository = executionRepository;
    }

    @Override
    public List<GlobalObjectDto> getAll() {
        return globalObjectDao.getAll().stream().map(this::buildDto).collect(Collectors.toList());
    }

    @Override
    public GlobalObjectDto get(int id) {
        GlobalObject object = globalObjectDao.get(id);

        if (object == null || object.isDeleted()) {
            return null;
        }

        return buildDto(object);
    }

    @Override
    public GlobalObjectDto create(ApplicationUser user, GlobalObjectForm form) {
        validate(true, form);

        GlobalObject result = globalObjectDao.createScript(user, form);

        invalidationService.invalidateGlobalObjects();

        return buildDto(result);
    }

    @Override
    public GlobalObjectDto update(ApplicationUser user, int id, GlobalObjectForm form) {
        validate(false, form);

        GlobalObject result = globalObjectDao.updateScript(user, id, form);

        invalidationService.invalidateGlobalObjects();

        return buildDto(result);
    }

    @Override
    public void delete(ApplicationUser user, int id) {
        globalObjectDao.deleteScript(user, id);
        invalidationService.invalidateGlobalObjects();
    }

    @Override
    public void restore(ApplicationUser user, int id) {
        globalObjectDao.restoreScript(user, id);
        invalidationService.invalidateGlobalObjects();
    }

    private void validate(boolean isNew, GlobalObjectForm form) {
        ValidationUtils.validateForm2(i18nHelper, isNew, form);

        if (StringUtils.isEmpty(form.getScriptBody())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "scriptBody");
        }

        scriptService.parseScriptStatic(form.getScriptBody(), ImmutableMap.of());

        if (globalObjectDao.getByName(form.getName()) != null) {
            throw new ValidationException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.nameTaken"), "name");
        }

        if (!Const.GLOBAL_OBJECT_NAME_PATTERN.matcher(form.getName()).matches()) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.incorrectGlobalObjectName"), "name");
        }
    }

    private GlobalObjectDto buildDto(GlobalObject script) {
        GlobalObjectDto result = new GlobalObjectDto();

        result.setId(script.getID());
        result.setUuid(script.getUuid());
        result.setName(script.getName());
        result.setDescription(script.getDescription());
        result.setScriptBody(script.getScriptBody());
        result.setDeleted(script.isDeleted());

        result.setChangelogs(changelogHelper.collect(script.getChangelogs()));

        result.setErrorCount(executionRepository.getErrorCount(script.getUuid()));
        result.setWarningCount(executionRepository.getWarningCount(script.getUuid()));

        return result;
    }
}
