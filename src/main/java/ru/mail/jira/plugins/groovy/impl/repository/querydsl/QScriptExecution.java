package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import java.sql.Timestamp;

public class QScriptExecution extends EnhancedRelationalPathBase<QScriptExecution> {
    public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();
    public final NumberPath<Integer> SCRIPT_ID = createIntegerCol("SCRIPT_ID").build();
    public final StringPath INLINE_ID = createStringCol("INLINE_ID").build();
    public final BooleanPath SUCCESSFUL = createBooleanCol("SUCCESSFUL").notNull().build();
    public final DateTimePath<Timestamp> DATE = createDateTimeCol("DATE", Timestamp.class).notNull().build();
    public final NumberPath<Long> TIME = createLongCol("TIME").notNull().build();

    public QScriptExecution() {
        super(QScriptExecution.class, "AO_2FC5DA_SCRIPT_EXECUTION");
    }
}
