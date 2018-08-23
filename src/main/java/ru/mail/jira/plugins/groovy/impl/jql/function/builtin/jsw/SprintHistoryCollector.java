package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.jsw;

import com.atlassian.greenhopper.customfield.sprint.SprintHistoryEntry;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import lombok.Getter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SprintHistoryCollector extends Collector {
    @Getter
    private final Set<String> issues = new HashSet<>();

    private final CustomField sprintField;
    private final Map<Long, DateTime> startDates;
    private final boolean collectAddition;

    private IndexReader indexReader;

    SprintHistoryCollector(CustomField sprintField, Map<Long, DateTime> startDates, boolean collectAddition) {
        this.sprintField = sprintField;
        this.startDates = startDates;
        this.collectAddition = collectAddition;
    }

    @Override
    public void setScorer(Scorer scorer) {

    }

    @Override
    public void collect(int doc) throws IOException {
        Document document = indexReader.document(doc);

        String[] values = document.getValues(sprintField.getId() + "_changes");
        if (values != null) {
            Map<Long, DateTime> lastAdditionDates = new HashMap<>();

            for (String value : values) {
                SprintHistoryEntry historyEntry = SprintHistoryEntry.fromLuceneValue(value);

                if (collectAddition) {
                    if (historyEntry.isAdded()) {
                        lastAdditionDates.put(historyEntry.getSprintId(), historyEntry.getDate());
                    } else {
                        lastAdditionDates.remove(historyEntry.getSprintId());
                    }
                } else {
                    if (historyEntry.isAdded()) {
                        lastAdditionDates.remove(historyEntry.getSprintId());
                    } else {
                        lastAdditionDates.put(historyEntry.getSprintId(), historyEntry.getDate());
                    }
                }
            }

            for (Long sprintId : lastAdditionDates.keySet()) {
                if (startDates.containsKey(sprintId) && lastAdditionDates.get(sprintId).isAfter(startDates.get(sprintId))) {
                    issues.add(document.get(DocumentConstants.ISSUE_ID));
                }
            }
        }
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) {
        indexReader = reader;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }
}
