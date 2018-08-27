package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QAbstractScript extends EnhancedRelationalPathBase<QAbstractScript> {
    public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();
    public final StringPath UUID = createStringCol("UUID").build();
    public final StringPath NAME = createStringCol("NAME").build();

    public QAbstractScript(String tableName) {
        super(QAbstractScript.class, "AO_2FC5DA_" + tableName);
    }
}
