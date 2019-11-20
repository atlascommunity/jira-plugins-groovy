package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.query;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.parser.antlr.JqlLexer;
import com.atlassian.jira.jql.parser.antlr.JqlParser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JqlFunctionParser {
    private static final Logger logger = LoggerFactory.getLogger(JqlFunctionParser.class);

    private final JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry;
    private final JqlDateSupport jqlDateSupport;
    private final UserKeyService userKeyService;
    private final UserManager userManager;


    public JqlFunctionParser(
        @ComponentImport JqlDateSupport jqlDateSupport,
        @ComponentImport UserKeyService userKeyService,
        @ComponentImport UserManager userManager
    ) {
        this.jqlFunctionHandlerRegistry = Preconditions.checkNotNull(
            ComponentAccessor.getComponent(JqlFunctionHandlerRegistry.class)
        );
        this.jqlDateSupport = jqlDateSupport;
        this.userKeyService = userKeyService;
        this.userManager = userManager;
    }

    public Set<String> parseUser(QueryCreationContext queryCreationContext, String value) {
        Set<String> result = null;

        if (isFunction(value)) {
            try {
                result = parseFunction(queryCreationContext, value)
                    .stream()
                    .map(QueryLiteral::getStringValue)
                    .map(userKeyService::getKeyForUsername)
                    .collect(Collectors.toSet());
            } catch (Exception e) {
                logger.warn("Unable to parse function", e);
            }
        } else {
            ApplicationUser user = userManager.getUserByName(value);
            if (user == null) {
                user = userManager.getUserByKey(value);
            }

            if (user != null) {
                result = ImmutableSet.of(user.getKey());
            }
        }

        return result;
    }

    public Date parseDate(QueryCreationContext queryCreationContext, String value) {
        Date result = null;

        if (isFunction(value)) {
            try {
                result = parseFunction(queryCreationContext, value)
                    .stream()
                    .map(QueryLiteral::getLongValue)
                    .filter(Objects::nonNull)
                    .map(Date::new)
                    .findAny()
                    .orElse(null);
            } catch (Exception e) {
                logger.warn("Unable to parse function", e);
            }
        } else if (jqlDateSupport.validate(value)) {
            result = jqlDateSupport.convertToDate(value);
        }

        return result;
    }

    private List<QueryLiteral> parseFunction(QueryCreationContext queryCreationContext, String functionCall) throws Exception {
        FunctionOperand functionOperand = createJqlParser(functionCall).func();
        FunctionOperandHandler operandHandler = jqlFunctionHandlerRegistry.getOperandHandler(functionOperand);

        return operandHandler.getValues(queryCreationContext, functionOperand, null);
    }

    //create JqlParser with jira class loader, since not all antlr packages are exported from jira core
    private JqlParser createJqlParser(String clauseString) throws Exception {
        ClassLoader jiraClassLoader = ClassLoaderUtil.getJiraClassLoader();

        Class<?> antlrStringStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.ANTLRStringStream");
        Class<?> commonTokenStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.CommonTokenStream");
        Class<?> charStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.CharStream");
        Class<?> tokenSourceClass = jiraClassLoader.loadClass("org.antlr.runtime.TokenSource");
        Class<?> tokenStreamClass = jiraClassLoader.loadClass("org.antlr.runtime.TokenStream");

        Object antlrStringStream = antlrStringStreamClass.getConstructor(String.class).newInstance(clauseString);
        JqlLexer lexer = JqlLexer.class.getConstructor(charStreamClass).newInstance(antlrStringStream);

        Object commonTokenStream = commonTokenStreamClass.getConstructor(tokenSourceClass).newInstance(lexer);
        return JqlParser.class.getConstructor(tokenStreamClass).newInstance(commonTokenStream);
    }

    private boolean isFunction(String value) {
        return value.indexOf('(') != -1 && value.indexOf(')') != -1;
    }
}
