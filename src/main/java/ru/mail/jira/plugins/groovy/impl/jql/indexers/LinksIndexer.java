package ru.mail.jira.plugins.groovy.impl.jql.indexers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;

import static com.atlassian.jira.issue.link.Direction.IN;
import static com.atlassian.jira.issue.link.Direction.OUT;

@Scanned
public class LinksIndexer implements FieldIndexer {
    public static final String LINKS_FIELD = "mrg_links";

    private final IssueLinkManager issueLinkManager;

    public LinksIndexer(
        @ComponentImport IssueLinkManager issueLinkManager
    ) {
        this.issueLinkManager = issueLinkManager;
    }

    @Override
    public String getId() {
        return LINKS_FIELD;
    }

    @Override
    public String getDocumentFieldId() {
        return LINKS_FIELD;
    }

    @Override
    public boolean isFieldVisibleAndInScope(Issue issue) {
        return true;
    }

    @Override
    public void addIndex(Document document, Issue issue) {
        Long issueId = issue.getId();

        addLinkField(document, issueLinkManager.getInwardLinks(issueId), IN);
        addLinkField(document, issueLinkManager.getOutwardLinks(issueId), OUT);
    }

    private void addLinkField(Document document, Collection<IssueLink> issueLinks, Direction direction) {
        for (IssueLink issueLink : issueLinks) {
            Long otherIssue = direction == Direction.OUT ? issueLink.getDestinationId() : issueLink.getSourceId();

            document.add(new Field(
                LINKS_FIELD,
                IssueLinkIndexer.createValue(issueLink.getLinkTypeId(), direction, otherIssue),
                Field.Store.YES, Field.Index.NOT_ANALYZED
            ));
        }
    }
}
