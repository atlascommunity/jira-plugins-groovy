package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import lombok.Getter;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.HashSet;
import java.util.Set;

public class AllVariableExpressionsVisitor extends ClassCodeVisitorSupport {
    @Getter
    private final Set<String> fields = new HashSet<>();

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (expression.getAccessedVariable() instanceof DynamicVariable) {
            fields.add(expression.getName());
        }
        super.visitVariableExpression(expression);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    public void clear() {
        fields.clear();
    }
}
