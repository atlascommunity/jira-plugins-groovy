package ru.mail.jira.plugins.groovy.impl.jql;

import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.ExactTextCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.validator.ExactTextCustomFieldValidator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.LastUpdatedByIndexer;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.LinksIndexer;

import java.util.concurrent.atomic.AtomicReference;

@Scanned
public class JqlFieldSearcher extends AbstractInitializationCustomFieldSearcher {
    private final FieldVisibilityManager fieldVisibilityManager;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final IssueLinkManager issueLinkManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final CustomClauseQueryFactory clauseQueryFactory;

    private CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private SearcherInformation<CustomField> searcherInformation;
    private SearchInputTransformer searchInputTransformer;
    private SearchRenderer searchRenderer;

    public JqlFieldSearcher(
        @ComponentImport FieldVisibilityManager fieldVisibilityManager,
        @ComponentImport CustomFieldInputHelper customFieldInputHelper,
        @ComponentImport IssueLinkManager issueLinkManager,
        @ComponentImport ChangeHistoryManager changeHistoryManager,
        CustomClauseQueryFactory clauseQueryFactory
    ) {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.customFieldInputHelper = customFieldInputHelper;
        this.issueLinkManager = issueLinkManager;
        this.changeHistoryManager = changeHistoryManager;
        this.clauseQueryFactory = clauseQueryFactory;
    }

    @Override
    public void init(CustomField field) {
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(
            new ExactTextCustomFieldValidator(),
            clauseQueryFactory,
            ImmutableSet.of(Operator.IN),
            new JiraDataTypeImpl(JqlFunctionCFType.class)
        );

        this.searcherInformation = new CustomFieldSearcherInformation(
            field.getId(), field.getNameKey(),
            ImmutableList.of(
                new LastUpdatedByIndexer(changeHistoryManager, fieldVisibilityManager),
                new LinksIndexer(issueLinkManager)
            ),
            new AtomicReference<>(field)
        );

        this.searchInputTransformer = new ExactTextCustomFieldSearchInputTransformer(
            field,
            field.getClauseNames(),
            searcherInformation.getId(),
            customFieldInputHelper
        );

        this.searchRenderer = new CustomFieldRenderer(
            field.getClauseNames(),
            getDescriptor(),
            field,
            new SingleValueCustomFieldValueProvider(),
            fieldVisibilityManager
        );
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        return customFieldSearcherClauseHandler;
    }

    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer() {
        return searchRenderer;
    }
}
