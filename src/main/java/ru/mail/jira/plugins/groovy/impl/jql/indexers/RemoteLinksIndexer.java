package ru.mail.jira.plugins.groovy.impl.jql.indexers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
public class RemoteLinksIndexer implements FieldIndexer {
    public final static String REMOTE_LINKS_FIELD = "mrg_remotelink";
    public final static String REMOTE_LINK_FIELD_APP_NAME = String.join("_", REMOTE_LINKS_FIELD, "app_name");
    public final static String REMOTE_LINK_FIELD_APP_TYPE = String.join("_", REMOTE_LINKS_FIELD, "app_type");
    public final static String REMOTE_LINK_FIELD_TITLE = String.join("_", REMOTE_LINKS_FIELD, "title");
    public final static String REMOTE_LINK_FIELD_URL = String.join("_", REMOTE_LINKS_FIELD, "url");
    public final static String REMOTE_LINK_FIELD_URL_HOST = String.join("_", REMOTE_LINKS_FIELD, "url_host");
    public final static String REMOTE_LINK_FIELD_URL_QUERY = String.join("_", REMOTE_LINKS_FIELD, "url_query");
    public final static String REMOTE_LINK_FIELD_URL_PATH = String.join("_", REMOTE_LINKS_FIELD, "url_path");
    public final static String REMOTE_LINK_FIELD_SUMMARY = String.join("_", REMOTE_LINKS_FIELD, "summary");
    public final static String REMOTE_LINK_FIELD_IS_RESOLVED = String.join("_", REMOTE_LINKS_FIELD, "is_resolved");
    public final static String REMOTE_LINK_FIELD_RELATIONSHIP = String.join("_", REMOTE_LINKS_FIELD, "relationship");
    public final static String REMOTE_LINK_FIELD_HAS_ANY = String.join("_", REMOTE_LINKS_FIELD, "has_any");

    private final RemoteIssueLinkManager remoteIssueLinkManager;

    public RemoteLinksIndexer(RemoteIssueLinkManager remoteIssueLinkManager) {
        this.remoteIssueLinkManager = remoteIssueLinkManager;
    }

    @Override
    public String getId() {
        return REMOTE_LINKS_FIELD;
    }

    @Override
    public String getDocumentFieldId() {
        return REMOTE_LINKS_FIELD;
    }

    @Override
    public void addIndex(Document doc, Issue issue) {
        remoteIssueLinkManager
                .getRemoteIssueLinksForIssue(issue)
                .forEach(remoteIssueLink -> {
                    Optional.ofNullable(remoteIssueLink.getTitle()).ifPresent(val -> doc.add(new TextField(REMOTE_LINK_FIELD_TITLE, val, Field.Store.NO)));
                    Optional.ofNullable(remoteIssueLink.getApplicationName()).ifPresent(val -> doc.add(new TextField(REMOTE_LINK_FIELD_APP_NAME, val, Field.Store.NO)));
                    Optional.ofNullable(remoteIssueLink.getApplicationType()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_APP_TYPE, val, Field.Store.NO)));
                    Optional.ofNullable(remoteIssueLink.getSummary()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_SUMMARY, val, Field.Store.NO)));
                    Optional.ofNullable(remoteIssueLink.getUrl()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_URL, val, Field.Store.NO)));
                    Optional.ofNullable(remoteIssueLink.getRelationship()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_URL, val, Field.Store.NO)));
                    Optional.ofNullable(remoteIssueLink.isResolved()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_URL, val ? "true" : "false", Field.Store.NO)));
                    try {
                        if (remoteIssueLink.getUrl() != null) {
                            URL parsedURL = new URL(remoteIssueLink.getUrl());
                            Optional.ofNullable(parsedURL.getHost()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_URL_HOST, val, Field.Store.NO)));
                            Optional.ofNullable(parsedURL.getQuery()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_URL_QUERY, val, Field.Store.NO)));
                            // need to check empty string here because URL.getPath() method returns empty string if path is empty
                            Optional.ofNullable(parsedURL.getPath()).filter(value -> !value.isEmpty()).ifPresent(val -> doc.add(new StringField(REMOTE_LINK_FIELD_URL_PATH, val, Field.Store.NO)));
                        }
                    } catch (MalformedURLException e) {
                        log.error(String.format("Unable to index url=%s field value in remote link", remoteIssueLink.getUrl()), e);
                    }
                });
        if (remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue).stream().anyMatch(link -> link.getTitle() != null)) {
            doc.add(new StringField(REMOTE_LINK_FIELD_HAS_ANY, "true", Field.Store.NO));
        }
    }

    @Override
    public boolean isFieldVisibleAndInScope(Issue issue) {
        return false;
    }
}
