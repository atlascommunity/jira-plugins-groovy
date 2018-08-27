package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QFieldConfig extends EnhancedRelationalPathBase<QFieldConfig> {
    public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();
    public final StringPath UUID = createStringCol("UUID").build();
    public final NumberPath<Integer> FIELD_CONFIG_ID = createIntegerCol("FIELD_CONFIG_ID").build();

    public QFieldConfig() {
        super(QFieldConfig.class, "AO_2FC5DA_FIELD_CONFIG");
    }
}
