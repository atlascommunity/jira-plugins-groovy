package ru.mail.jira.plugins.groovy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@AllArgsConstructor @XmlRootElement @Getter
public class CacheStatsDto {
    @XmlElement
    private final long hitCount;
    @XmlElement
    private final long missCount;
    @XmlElement
    private final long loadSuccessCount;
    @XmlElement
    private final long loadFailureCount;
    @XmlElement
    private final long totalLoadTime;
    @XmlElement
    private final long evictionCount;
    @XmlElement
    private final long evictionWeight;
    @XmlElement
    private final long estimatedSize;
}
