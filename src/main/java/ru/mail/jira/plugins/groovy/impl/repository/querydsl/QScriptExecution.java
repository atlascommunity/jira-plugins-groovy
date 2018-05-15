package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;

public class QScriptExecution extends EnhancedRelationalPathBase<QScriptExecution> {
    public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();
    public final NumberPath<Integer> SCRIPT_ID = createIntegerCol("SCRIPT_ID").build();
    public final BooleanPath SUCCESSFUL = createBooleanCol("SUCCESSFUL").notNull().build();

    public QScriptExecution() {
        super(QScriptExecution.class, "AO_2FC5DA_SCRIPT_EXECUTION");
    }
}
