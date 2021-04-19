package ru.mail.jira.plugins.groovy.impl.jql.function.builtin;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.impl.jql.indexers.RemoteLinksIndexer;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Component
public class LinkedIssuesOfRemoteFunction extends AbstractBuiltInQueryFunction {
    @Autowired
    public LinkedIssuesOfRemoteFunction() {
        super("linkedIssuesOfRemote", 1);
    }

    @Override
    protected void validate(MessageSet messageSet, ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        if (functionOperand.getArgs().size() > 2 || functionOperand.getArgs().size() < 1) {
            messageSet.addWarningMessage("Usage: linkedIssuesOfRemote(\"seacthType\", \"searchParam\") or linkedIssuesOfRemote(\"remoteLinkUrl\") or linkedIssuesOfRemote(\"remoteLinkTitleWildcard\")");
        }
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        Operand operand = terminalClause.getOperand();
        Operator operator = terminalClause.getOperator();
        if (operator.equals(Operator.IN) || operator.equals(Operator.NOT_IN)) {
            if (operand instanceof FunctionOperand) {
                FunctionOperand functionOperand = (FunctionOperand) operand;
                if (functionOperand.getArgs().size() == 1) {
                    String firstArg = functionOperand.getArgs().get(0);
                    BooleanQuery.Builder builder = new BooleanQuery.Builder();
                    try {
                        // check URL is correct
                        URL parsedUrl = new URL(firstArg);
                        builder.add(new PrefixQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_URL, firstArg)), BooleanClause.Occur.MUST);
                    } catch (MalformedURLException e) {
                        builder.add(new WildcardQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_TITLE, firstArg)), BooleanClause.Occur.MUST);
                    }
                    return new QueryFactoryResult(builder.build(), operator.equals(Operator.NOT_IN));
                }
                if (functionOperand.getArgs().size() == 2) {
                    String firstArg = functionOperand.getArgs().get(0);
                    String secondArg = functionOperand.getArgs().get(1);
                    BooleanQuery.Builder builder = new BooleanQuery.Builder();
                    switch (firstArg.toLowerCase()) {
                        case "application name":
                            builder.add(new PrefixQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_APP_NAME, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "application type":
                            builder.add(new PrefixQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_APP_TYPE, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "query":
                            builder.add(new PrefixQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_URL_QUERY, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "host":
                            builder.add(new PrefixQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_URL_HOST, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "path":
                            builder.add(new WildcardQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_URL_PATH, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "relationship":
                            builder.add(new TermQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_RELATIONSHIP, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "summary":
                            builder.add(new PrefixQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_SUMMARY, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        case "is resolved":
                            builder.add(new TermQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_IS_RESOLVED, secondArg.equals("YES") ? "true" : "false")), BooleanClause.Occur.MUST);
                            break;
                        case "title":
                            builder.add(new WildcardQuery(new Term(RemoteLinksIndexer.REMOTE_LINK_FIELD_TITLE, secondArg)), BooleanClause.Occur.MUST);
                            break;
                        default:
                            return QueryFactoryResult.createFalseResult();
                    }
                    return new QueryFactoryResult(builder.build(), operator.equals(Operator.NOT_IN));
                }
            }
            return QueryFactoryResult.createFalseResult();
        }
        return QueryFactoryResult.createFalseResult();
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return ImmutableList.of();
    }
}
