package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.*;

import java.sql.Timestamp;

public class QAuditLogEntry extends EnhancedRelationalPathBase<QAuditLogEntry> {
    public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();
    public final NumberPath<Integer> ENTITY_ID = createIntegerCol("ENTITY_ID").build();
    public final StringPath USER_KEY = createStringCol("USER_KEY").notNull().build();
    //have to use string for enum, because value is not converted from string to enum when getting value from QueryDSL tuple
    public final StringPath CATEGORY = createStringCol("CATEGORY").notNull().build();
    public final StringPath ACTION = createStringCol("ACTION").notNull().build();
    public final StringPath DESCRIPTION = createStringCol("DESCRIPTION").build();
    public final DateTimePath<Timestamp> DATE = createDateTimeCol("DATE", Timestamp.class).notNull().build();

    public QAuditLogEntry() {
        super(QAuditLogEntry.class, "AO_2FC5DA_AUDIT_LOG_ENTRY");
    }
}
