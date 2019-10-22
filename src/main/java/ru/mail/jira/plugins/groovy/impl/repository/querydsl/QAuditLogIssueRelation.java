package ru.mail.jira.plugins.groovy.impl.repository.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.NumberPath;

public class QAuditLogIssueRelation extends EnhancedRelationalPathBase<QAuditLogIssueRelation> {
    public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();
    public final NumberPath<Integer> AUDIT_LOG_ID = createIntegerCol("AUDIT_LOG_ID").build();
    public final NumberPath<Long> ISSUE_ID = createLongCol("ISSUE_ID").build();

    public QAuditLogIssueRelation() {
        super(QAuditLogIssueRelation.class, "AO_2FC5DA_AUDIT_ISSUE_REL");
    }
}
