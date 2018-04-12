package ru.mail.jira.plugins.groovy.util;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.dto.ChangelogDto;
import ru.mail.jira.plugins.groovy.api.dto.JiraIssueReference;
import ru.mail.jira.plugins.groovy.api.entity.AbstractChangelog;
import ru.mail.jira.plugins.groovy.api.entity.FieldConfigChangelog;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public final class ChangelogHelper {
    private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)");

    private final DateTimeFormatter dateTimeFormatter;
    private final IssueManager issueManager;
    private final ActiveObjects ao;
    private final UserMapper userMapper;

    @Autowired
    public ChangelogHelper(
        @ComponentImport DateTimeFormatter dateTimeFormatter,
        @ComponentImport IssueManager issueManager,
        @ComponentImport ActiveObjects ao,
        UserMapper userMapper
    ) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.issueManager = issueManager;
        this.ao = ao;
        this.userMapper = userMapper;
    }

    public String generateDiff(long id, String originalName, String name, String originalSource, String newSource) {
        if (originalSource == null) {
            originalSource = "";
        }
        if (newSource == null) {
            newSource = "";
        }

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

    private ChangelogDto buildDto(AbstractChangelog changelog) {
        ChangelogDto result = new ChangelogDto();

        result.setId(changelog.getID());
        result.setAuthor(userMapper.buildUser(changelog.getAuthorKey()));
        result.setComment(changelog.getComment());
        result.setDiff(changelog.getDiff());
        result.setDate(dateTimeFormatter.forLoggedInUser().format(changelog.getDate()));

        if (changelog instanceof FieldConfigChangelog) {
            result.setTemplateDiff(((FieldConfigChangelog) changelog).getTemplateDiff());
        }

        Set<JiraIssueReference> issueReferences = new HashSet<>();

        Matcher issueKeyMatcher = ISSUE_KEY_PATTERN.matcher(changelog.getComment());
        while (issueKeyMatcher.find()) {
            String issueKey = issueKeyMatcher.group();

            Issue issue = issueManager.getIssueByCurrentKey(issueKey);
            if (issue != null) {
                issueReferences.add(new JiraIssueReference(issue.getKey(), issue.getSummary()));
            }
        }

        result.setIssueReferences(issueReferences);

        return result;
    }

    public List<ChangelogDto> collect(AbstractChangelog[] changelogs) {
        if (changelogs != null) {
            return Arrays
                .stream(changelogs)
                .sorted(Comparator.comparing(AbstractChangelog::getDate).reversed())
                .map(this::buildDto)
                .collect(Collectors.toList());
        }
        return null;
    }

    public void addChangelog(Class<? extends AbstractChangelog> clazz, int scriptId, String userKey, String diff, String comment) {
        addChangelog(clazz, "SCRIPT_ID", scriptId, userKey, diff, comment);
    }

    public void addChangelog(Class<? extends AbstractChangelog> clazz, String fkField, int scriptId, String userKey, String diff, String comment) {
        addChangelog(clazz, fkField, scriptId, userKey, diff, comment, ImmutableMap.of());
    }

    public void addChangelog(
        Class<? extends AbstractChangelog> clazz,
        String fkField,
        int scriptId,
        String userKey,
        String diff,
        String comment,
        Map<String, Object> additionalParams
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("AUTHOR_KEY", userKey);
        params.put(fkField, scriptId);
        params.put("DATE", new Timestamp(System.currentTimeMillis()));
        params.put("DIFF", StringUtils.isEmpty(diff) ? "no changes" : diff);
        params.put("COMMENT", comment);
        params.putAll(additionalParams);

        ao.create(
            clazz,
            params
        );
    }

    private static String genName(long id, String name) {
        return String.valueOf(id) + " - " + name + ".groovy";
    }
}
