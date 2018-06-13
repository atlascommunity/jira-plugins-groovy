package ru.mail.jira.plugins.groovy.api.dto;

import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@XmlRootElement
public class Page<T> {
    @XmlElement
    private final int offset;
    @XmlElement
    private final int limit;
    @XmlElement
    private final long total;
    @XmlElement
    private final int size;
    @XmlElement
    private final boolean isLast;
    @XmlElement
    private final List<T> values;

    public Page(int offset, int limit, long total, List<T> values) {
        this.offset = offset;
        this.limit = limit;
        this.total = total;
        this.size = values.size();
        this.isLast = offset + this.size == total;
        this.values = values;
    }
}
