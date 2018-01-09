package ru.mail.jira.plugins.groovy.impl.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.groovy.api.ScriptRepository;
import ru.mail.jira.plugins.groovy.api.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.*;
import ru.mail.jira.plugins.groovy.api.entity.Changelog;
import ru.mail.jira.plugins.groovy.api.entity.Script;
import ru.mail.jira.plugins.groovy.api.entity.ScriptDirectory;
import ru.mail.jira.plugins.groovy.api.dto.ScriptDescription;
import ru.mail.jira.plugins.groovy.impl.ScriptInvalidationService;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScriptRepositoryImpl implements ScriptRepository {
    private final I18nHelper i18nHelper;
    private final DateTimeFormatter dateTimeFormatter;
    private final JiraAuthenticationContext authenticationContext;
    private final UserManager userManager;
    private final AvatarService avatarService;
    private final ClusterLockService clusterLockService;
    private final ActiveObjects ao;
    private final ScriptInvalidationService scriptInvalidationService;
    private final ScriptService scriptService;

    @Autowired
    public ScriptRepositoryImpl(
        @ComponentImport I18nHelper i18nHelper,
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        @ComponentImport JiraAuthenticationContext authenticationContext,
        @ComponentImport UserManager userManager,
        @ComponentImport AvatarService avatarService,
        @ComponentImport ClusterLockService clusterLockService,
        @ComponentImport ActiveObjects ao,
        ScriptInvalidationService scriptInvalidationService,
        ScriptService scriptService
    ) {
        this.i18nHelper = i18nHelper;
        this.dateTimeFormatter = dateTimeFormatter;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.avatarService = avatarService;
        this.clusterLockService = clusterLockService;
        this.ao = ao;
        this.scriptInvalidationService = scriptInvalidationService;
        this.scriptService = scriptService;
    }

    private ScriptDirectory getParentDirectory(Integer parentId) {
        ScriptDirectory parentDirectory = null;

        if (parentId != null) {
            parentDirectory = ao.get(ScriptDirectory.class, parentId);

            if (parentDirectory == null) {
                throw new IllegalArgumentException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.directoryNotFound", parentId));
            } else if (parentDirectory.isDeleted()) {
                throw new IllegalArgumentException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.parentDirectoryIsDeleted"));
            }
        }

        return parentDirectory;
    }

    @Override
    public List<ScriptDirectoryTreeDto> getAllDirectories() {
        Multimap<Integer, ScriptDto> scripts = HashMultimap.create();
        for (ScriptDto scriptDto : getAllScripts()) {
            scripts.put(scriptDto.getDirectoryId(), scriptDto);
        }

        return Arrays
            .stream(ao.find(ScriptDirectory.class, Query.select().where("DELETED = ? AND PARENT_ID IS NULL", Boolean.FALSE)))
            .map(directory -> buildDirectoryTreeDto(directory, scripts))
            .collect(Collectors.toList());
    }

    @Override
    public ScriptDirectoryDto getDirectory(int id) {
        return buildDirectoryDto(ao.get(ScriptDirectory.class, id));
    }

    @Override
    public ScriptDirectoryDto createDirectory(ApplicationUser user, ScriptDirectoryForm directoryForm) {
        validateDirectoryForm(directoryForm);

        return buildDirectoryDto(ao.create(
            ScriptDirectory.class,
            new DBParam("NAME", directoryForm.getName()),
            new DBParam("PARENT_ID", getParentDirectory(directoryForm.getParentId())),
            new DBParam("DELETED", false)
        ));
    }

    @Override
    public ScriptDirectoryDto updateDirectory(ApplicationUser user, int id, ScriptDirectoryForm directoryForm) {
        ScriptDirectory scriptDirectory = ao.get(ScriptDirectory.class, id);
        scriptDirectory.setName(directoryForm.getName());
        scriptDirectory.setParent(getParentDirectory(directoryForm.getParentId()));
        scriptDirectory.save();

        return buildDirectoryDto(scriptDirectory);
    }

    @Override
    public void deleteDirectory(ApplicationUser user, int id) {
        deleteDirectory(user, ao.get(ScriptDirectory.class, id));
    }

    private void deleteDirectory(ApplicationUser user, ScriptDirectory scriptDirectory) {
        scriptDirectory.setDeleted(true);
        scriptDirectory.save();

        for (ScriptDirectory child : scriptDirectory.getChildren()) {
            deleteDirectory(user, child);
        }

        for (Script script : scriptDirectory.getScripts()) {
            deleteScript(user, script);
        }
    }

    @Override
    public List<ScriptDto> getAllScripts() {
        return Arrays
            .stream(ao.find(Script.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(this::buildScriptDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<ScriptDescription> getAllScriptDescriptions() {
        return Arrays
            .stream(ao.find(Script.class, Query.select().where("DELETED = ?", Boolean.FALSE)))
            .map(ScriptRepositoryImpl::buildScriptDescription)
            .sorted(Comparator.comparing(ScriptDescription::getName))
            .collect(Collectors.toList());
    }

    @Override
    public Script getRawScript(int id) {
        return ao.get(Script.class, id);
    }

    @Override
    public ScriptDto getScript(int id) {
        return buildScriptDto(ao.get(Script.class, id));
    }

    @Override
    public ScriptDto createScript(ApplicationUser user, ScriptForm scriptForm) {
        if (scriptForm.getDirectoryId() == null) {
            throw new RuntimeException();
        }

        validateScriptForm(true, scriptForm);

        Script script = ao.create(
            Script.class,
            new DBParam("NAME", scriptForm.getName()),
            new DBParam("SCRIPT_BODY", scriptForm.getScriptBody()),
            new DBParam("DIRECTORY_ID", scriptForm.getDirectoryId()),
            new DBParam("DELETED", false)
        );

        String diff = generateDiff(script.getID(), "", script.getName(), "", scriptForm.getScriptBody());

        ao.create(
            Changelog.class,
            new DBParam("AUTHOR_KEY", user.getKey()),
            new DBParam("SCRIPT_ID", script.getID()),
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("DIFF", diff),
            new DBParam("COMMENT", "Created.")
        );

        return buildScriptDto(script);
    }

    @Override
    public ScriptDto updateScript(ApplicationUser user, int id, ScriptForm scriptForm) {
        validateScriptForm(false, scriptForm);

        ClusterLock lock = clusterLockService.getLockForName(getLockKey(id));

        lock.lock();
        try {
            ScriptDto result = doUpdateScript(user, id, scriptForm);
            scriptInvalidationService.invalidate(String.valueOf(id));
            return result;
        } finally {
            lock.unlock();
        }
    }

    private ScriptDto doUpdateScript(ApplicationUser user, int id, ScriptForm form) {
        Script script = ao.get(Script.class, id);

        if (script.isDeleted()) {
            throw new IllegalArgumentException("Script " + id + " is deleted");
        }

        String diff = generateDiff(id, script.getName(), form.getName(), script.getScriptBody(), form.getScriptBody());

        ao.create(
            Changelog.class,
            new DBParam("AUTHOR_KEY", user.getKey()),
            new DBParam("SCRIPT_ID", script.getID()),
            new DBParam("DATE", new Timestamp(System.currentTimeMillis())),
            new DBParam("DIFF", diff),
            new DBParam("COMMENT", form.getComment())
        );

        script.setName(form.getName());
        script.setScriptBody(form.getScriptBody());
        script.save();

        return buildScriptDto(script);
    }

    private String generateDiff(int id, String originalName, String name, String originalSource, String newSource) {
        try {
            List<String> originalLines = Arrays.asList(originalSource.split("\n"));
            List<String> newLines = Arrays.asList(newSource.split("\n"));
            Patch<String> patch = DiffUtils.diff(originalLines, newLines);

            return UnifiedDiffUtils
                .generateUnifiedDiff(genName(id, originalName), genName(id, name), originalLines, patch, 5)
                .stream()
                .collect(Collectors.joining("\n"));
        } catch (DiffException e) {
            throw new RuntimeException("Unable to create diff", e);
        }
    }

    private static String genName(int id, String name) {
        return String.valueOf(id) + " - " + name + ".groovy";
    }

    @Override
    public void deleteScript(ApplicationUser user, int id) {
        ClusterLock lock = clusterLockService.getLockForName(getLockKey(id));

        lock.lock();
        try {
            deleteScript(user, ao.get(Script.class, id));
        } finally {
            lock.unlock();
        }
    }

    private void deleteScript(ApplicationUser user, Script script) {
        script.setDeleted(true);
        script.save();
    }

    private ScriptDto buildScriptDto(Script script) {
        ScriptDto result = new ScriptDto();

        result.setId(script.getID());
        result.setName(script.getName());
        result.setDirectoryId(script.getDirectory().getID());
        result.setScriptBody(script.getScriptBody());
        result.setDeleted(script.isDeleted());

        Changelog[] changelogs = script.getChangelogs();
        if (changelogs != null) {
            result.setChangelogs(
                Arrays
                    .stream(changelogs)
                    .sorted(Comparator.comparing(Changelog::getDate).reversed())
                    .map(this::buildChangelogDto)
                    .collect(Collectors.toList())
            );
        }

        return result;
    }

    private static ScriptDirectoryDto buildDirectoryDto(ScriptDirectory directory) {
        ScriptDirectoryDto result = new ScriptDirectoryDto();

        result.setId(directory.getID());
        result.setName(directory.getName());

        ScriptDirectory parent = directory.getParent();
        if (parent != null) {
            result.setParentId(parent.getID());
        }

        return result;
    }

    private static ScriptDirectoryTreeDto buildDirectoryTreeDto(ScriptDirectory directory, Multimap<Integer, ScriptDto> scripts) {
        ScriptDirectoryTreeDto result = new ScriptDirectoryTreeDto();

        result.setId(directory.getID());
        result.setName(directory.getName());
        result.setChildren(
            Arrays
                .stream(directory.getChildren())
                .map(child -> buildDirectoryTreeDto(child, scripts))
                .collect(Collectors.toList())
        );
        result.setScripts(scripts.get(directory.getID()).stream().sorted(Comparator.comparing(ScriptDto::getId)).collect(Collectors.toList()));

        return result;
    }

    private ChangelogDto buildChangelogDto(Changelog changelog) {
        ChangelogDto result = new ChangelogDto();

        result.setId(changelog.getID());
        result.setAuthor(buildUser(changelog.getAuthorKey()));
        result.setComment(changelog.getComment());
        result.setDiff(changelog.getDiff());
        result.setDate(dateTimeFormatter.forLoggedInUser().format(changelog.getDate()));

        return result;
    }

    private JiraUser buildUser(String key) {
        ApplicationUser user = userManager.getUserByKey(key);

        if (user == null) {
            return new JiraUser(key, null, key);
        }

        return new JiraUser(
            user.getName(),
            avatarService.getAvatarURL(authenticationContext.getLoggedInUser(), user).toString(),
            user.getDisplayName()
        );
    }

    private void validateDirectoryForm(ScriptDirectoryForm form) {
        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }
    }

    private void validateScriptForm(boolean isNew, ScriptForm form) {
        scriptService.validateScript(form.getScriptBody());

        if (StringUtils.isEmpty(form.getName())) {
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "name");
        }

        if (!isNew) {
            if (StringUtils.isEmpty(form.getComment())) {
                throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.groovy.error.fieldRequired"), "comment");
            }
        }
    }

    private static ScriptDescription buildScriptDescription(Script script) {
        ScriptDescription result = new ScriptDescription();
        result.setId(script.getID());

        List<String> nameElements = new ArrayList<>();
        nameElements.add(script.getName());

        ScriptDirectory directory = script.getDirectory();
        while (directory != null) {
            nameElements.add(directory.getName());
            directory = directory.getParent();
        }

        result.setName(Lists.reverse(nameElements).stream().collect(Collectors.joining("/")));

        return result;
    }

    private static String getLockKey(int id) {
        return ScriptRepositoryImpl.class.toString() + "_script_" + id;
    }
}
