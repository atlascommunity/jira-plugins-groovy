package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QAbstractChangelog extends EnhancedRelationalPathBase<QAbstractChangelog> {
    public final StringPath UUID = createStringCol("UUID").build();
    public final NumberPath<Integer> SCRIPT_ID;

    public QAbstractChangelog(String tableName, String scriptColumn) {
        super(QAbstractChangelog.class, "AO_2FC5DA_" + tableName);

        this.SCRIPT_ID = createIntegerCol(scriptColumn).build();
    }

    public QAbstractChangelog(String tableName) {
        this(tableName, "SCRIPT_ID");
    }
}
