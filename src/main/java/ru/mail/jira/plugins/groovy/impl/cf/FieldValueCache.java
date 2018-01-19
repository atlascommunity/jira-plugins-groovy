package ru.mail.jira.plugins.groovy.impl.cf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class FieldValueCache {
    private final Logger logger = LoggerFactory.getLogger(FieldValueCache.class);
    private final Cache<CacheKey, ValueHolder> cache = Caffeine
        .newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(5000)
        .build();

    public Cache<CacheKey, ValueHolder> get() {
        return cache;
    }

    //todo: invalidate on issue change ?
    //todo: use
    public void invalidateField(long fieldId) {
        logger.debug("invalidating values for field {}", fieldId);
        cache
            .asMap()
            .keySet()
            .stream()
            .filter(key -> key.getFieldId() == fieldId)
            .forEach(cache::invalidate);
    }

    public void invalidateAll() {
        logger.debug("invalidating all values");
        cache.invalidateAll();
    }

    @AllArgsConstructor
    @Getter
    public static class ValueHolder {
        private final long lastModified;
        private final Object value;
    }

    @Getter
    @AllArgsConstructor
    public static final class CacheKey {
        private final long fieldId;
        private final long issueId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey that = (CacheKey) o;
            return fieldId == that.fieldId &&
                issueId == that.issueId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldId, issueId);
        }

        @Override
        public String toString() {
            return "ScriptFieldValueCacheKey{" +
                "fieldId=" + fieldId +
                ", issueId=" + issueId +
                '}';
        }
    }
}
