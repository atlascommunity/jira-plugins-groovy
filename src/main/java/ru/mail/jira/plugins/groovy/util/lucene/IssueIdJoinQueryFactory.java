package ru.mail.jira.plugins.groovy.util.lucene;

import com.atlassian.jira.issue.search.SearchProviderFactory;
import java.io.IOException;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IssueIdJoinQueryFactory {
    private static final Logger log = LoggerFactory.getLogger(IssueIdJoinQueryFactory.class);
    private final SearchProviderFactory searchProviderFactory;

    @Autowired
    public IssueIdJoinQueryFactory(
        @ComponentImport SearchProviderFactory searchProviderFactory
    ) {
        this.searchProviderFactory = searchProviderFactory;
    }

    public Query createIssueIdJoinQuery(Query indexQuery, String indexName) {
        try {
            return JoinUtil.createJoinQuery(
                "issue_id", false, "issue_id", indexQuery,
                this.searchProviderFactory.getSearcher(indexName),
                ScoreMode.None
            );
        } catch (IOException var4) {
            log.error("Unable to search the " + indexName + " index.", var4);
            return new MatchNoDocsQuery();
        }
    }
}
