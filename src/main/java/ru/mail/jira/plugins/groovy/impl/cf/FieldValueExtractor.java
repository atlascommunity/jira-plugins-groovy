package ru.mail.jira.plugins.groovy.impl.cf;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.api.repository.ExecutionRepository;
import ru.mail.jira.plugins.groovy.api.repository.FieldConfigRepository;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.api.dto.cf.FieldScript;
import ru.mail.jira.plugins.groovy.api.script.ScriptType;
import ru.mail.jira.plugins.groovy.impl.groovy.statik.TypeUtil;
import ru.mail.jira.plugins.groovy.util.Const;
import ru.mail.jira.plugins.groovy.util.ExceptionHelper;

import java.util.HashMap;
import java.util.Map;

@Component
public class FieldValueExtractor {
    private final Logger logger = LoggerFactory.getLogger(FieldValueExtractor.class);

    private final FieldConfigRepository fieldConfigRepository;
    private final ScriptService scriptService;
    private final ExecutionRepository executionRepository;
    private final FieldValueCache cache;
    private final ThreadLocal<Boolean> gettingCacheableValue = ThreadLocal.withInitial(() -> Boolean.FALSE);

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

    public FieldScript getScript(CustomField field, Issue issue) {
        FieldConfig config = field.getRelevantConfig(issue);

        if (config != null) {
            return fieldConfigRepository.getScript(config.getId());
        }
        return null;
    }

    public ValueHolder preview(Issue issue, CustomField field, FieldScript script) {
        CustomFieldSearcher searcher = field.getCustomFieldSearcher();
        Class type = searcher != null ?
            Const.SEARCHER_TYPES.getOrDefault(searcher.getDescriptor().getCompleteKey(), Object.class) : Object.class;
        return extractValueHolder(script, field, issue, type);
    }

    public <T> T extractValue(CustomField field, Issue issue, Class<T> tType) {
        ValueHolder valueHolder = extractValueHolder(field, issue, tType);

        if (valueHolder != null) {
            return tType.cast(valueHolder.getValue());
        }

        return null;
    }

    public ValueHolder extractValueHolder(CustomField field, Issue issue, Class tType) {
        if (gettingCacheableValue.get()) {
            throw new IllegalStateException("Trying to extract value from script of other field");
        }

        FieldScript script = getScript(field, issue);

        return extractValueHolder(script, field, issue, tType);
    }

    private ValueHolder extractValueHolder(FieldScript script, CustomField field, Issue issue, Class tType) {
        if (script != null && script.getScriptBody() != null && script.getId() != null) {
            if (script.isCacheable()) {
                try {
                    gettingCacheableValue.set(true);
                    //divide by 1000 because value is stored in index is divided by 1000
                    long lastModified = issue.getUpdated().getTime() / 1000;
                    FieldValueCache.CacheKey key = new FieldValueCache.CacheKey(field.getIdAsLong(), issue.getId());

                    ValueHolder cachedValue = getCachedValue(key, field, issue, lastModified, script, tType);
                    if (cachedValue != null) {
                        if (cachedValue.getLastModified() != lastModified) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("invalidating stale value of field {} for issue {}", field.getId(), issue.getKey());
                            }
                            cache.get().invalidate(key);
                            getCachedValue(key, field, issue, lastModified, script, tType);
                        }

                        return cachedValue;
                    }
                } finally {
                    gettingCacheableValue.remove();
                }
            } else {
                boolean isTemplated = field.getCustomFieldType() instanceof TemplateScriptedCFType;

                Map<String, Object> velocityParams = isTemplated ? new HashMap<>() : null;

                return new ValueHolder(
                    issue.getUpdated().getTime()/1000,
                    doExtractValue(field, script, issue, velocityParams, tType),
                    velocityParams
                );
            }
        }

        return null;
    }

    private <T> ValueHolder getCachedValue(
        FieldValueCache.CacheKey key,
        CustomField field,
        Issue issue,
        long lastModified,
        FieldScript script,
        Class<T> tType
    ) {
        boolean isTemplated = field.getCustomFieldType() instanceof TemplateScriptedCFType;

        return cache.get().get(
            key, (ignore) -> {
                Map<String, Object> velocityParams = null;
                if (isTemplated) {
                    velocityParams = new HashMap<>();
                }
                T value = doExtractValue(field, script, issue, velocityParams, tType);
                return new ValueHolder<Object>(lastModified, value, velocityParams);
            }
        );
    }

    private <T> T doExtractValue(CustomField field, FieldScript script, Issue issue, Map<String, Object> velocityParams, Class<T> tType) {
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

            Map<String, Object> bindings = new HashMap<>();
            bindings.put("issue", issue);
            bindings.put("velocityParams", velocityParams);
            Object result;
            if (script.isCompileStatic()) {
                result = scriptService.executeScriptStatic(
                    script.getId(),
                    script.getScriptBody(),
                    ScriptType.CUSTOM_FIELD,
                    bindings,
                    TypeUtil.getFieldConfigTypes(script.isWithVelocityParams())
                );
            } else {
                result = scriptService.executeScript(
                    script.getId(),
                    script.getScriptBody(),
                    ScriptType.CUSTOM_FIELD,
                    bindings
                );
            }

            if (result == null) {
                return null;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("script result {}", result);
            }

            if (tType == Double.class && (result instanceof Number) && !(result instanceof Double)) {
                result = ((Number) result).doubleValue();
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

    public void clearCache() {
        cache.invalidateAll();
    }
}
