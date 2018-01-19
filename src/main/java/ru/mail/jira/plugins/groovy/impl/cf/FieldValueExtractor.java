package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;

@Component
public class FieldValueExtractor {
    private final Logger logger = LoggerFactory.getLogger(FieldValueExtractor.class);

    private final FieldConfigRepository fieldConfigRepository;
    private final ScriptService scriptService;
    private final ExecutionRepository executionRepository;
    private final FieldValueCache cache;
    private final ThreadLocal<Boolean> gettingValue = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Autowired
    public FieldValueExtractor(
        FieldConfigRepository fieldConfigRepository,
        ScriptService scriptService,
        ExecutionRepository executionRepository,
        FieldValueCache cache
    ) {
        this.fieldConfigRepository = fieldConfigRepository;
        this.scriptService = scriptService;
        this.executionRepository = executionRepository;
        this.cache = cache;
    }

    public <T> T extractValue(CustomField field, Issue issue, Class<T> tType) {
        if (gettingValue.get()) {
            throw new IllegalStateException("Trying to extract value from script of other field");
        }

        FieldConfig config = field.getRelevantConfig(issue);

        if (config != null) {
            FieldScript script = fieldConfigRepository.getScript(config.getId());

            if (script != null && script.getScriptBody() != null && script.getId() != null) {
                try {
                    gettingValue.set(true);
                    if (script.isCacheable()) {
                        //divide by 1000 because value is stored in index is divided by 1000
                        long lastModified = issue.getUpdated().getTime() / 1000;
                        FieldValueCache.CacheKey key = new FieldValueCache.CacheKey(field.getIdAsLong(), issue.getId());

                        FieldValueCache.ValueHolder cachedValue = getCachedValue(key, field, issue, lastModified, script, tType);
                        if (cachedValue != null) {
                            if (cachedValue.getLastModified() != lastModified) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("invalidating stale value of field {} for issue {}", field.getId(), issue.getKey());
                                }
                                cache.get().invalidate(key);
                                getCachedValue(key, field, issue, lastModified, script, tType);
                            }

                            return tType.cast(cachedValue.getValue());
                        }
                    } else {
                        return doExtractValue(field, script, issue, tType);
                    }
                } finally {
                    gettingValue.remove();
                }
            }
        }

        return null;
    }

    private <T> FieldValueCache.ValueHolder getCachedValue(
        FieldValueCache.CacheKey key,
        CustomField field,
        Issue issue,
        long lastModified,
        FieldScript script,
        Class<T> tType
    ) {
        return cache.get().get(
            key, (ignore) -> new FieldValueCache.ValueHolder(lastModified, doExtractValue(field, script, issue, tType))
        );
    }

    private <T> T doExtractValue(CustomField field, FieldScript script, Issue issue, Class<T> tType) {
        if (logger.isTraceEnabled()) {
            logger.trace("Extracting value from issue {} for field {} (cacheable: {})", issue.getKey(), field.getId(), script.isCacheable());
        }

        String uuid = script.getId();
        long t = System.currentTimeMillis();
        boolean successful = true;
        String error = null;
        T value = null;

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("executing script for field {} with id {}", field.getId(), script.getId());
            }

            Object result = scriptService.executeScript(
                script.getId(),
                script.getScriptBody(),
                ScriptType.CUSTOM_FIELD,
                ImmutableMap.of("issue", issue)
            );

            if (result == null) {
                return null;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("script result {}", result);
            }

            if (!tType.isInstance(result)) {
                logger.error("Result type ({}) doesn't match field type {}", result.getClass(), tType);
            }
            //todo: try to check collections in future if multi

            value = tType.cast(result);
        } catch (Exception e) {
            logger.error(
                "caught exception in script field {} for issue {}",
                field.getIdAsLong(), issue.getKey(), e
            );
            successful = false;
            error = ExceptionHelper.writeExceptionToString(e);
        }

        //todo: make an option?
        if (!successful) {
            executionRepository.trackInline(uuid, System.currentTimeMillis() - t, successful, error, ImmutableMap.of(
                "issue", issue.getKey(),
                "type", ScriptType.CUSTOM_FIELD.name()
            ));
        }

        return value;
    }
}
