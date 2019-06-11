package ru.mail.jira.plugins.groovy.impl.groovy;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;
import java.util.Set;

public class WithPluginVisitor extends ClassCodeVisitorSupport {
    private final ParseContextHolder parseContextHolder;

    public WithPluginVisitor(ParseContextHolder parseContextHolder) {
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);

        processAnnotations(node.getAnnotations());
    }

    private void processAnnotations(List<AnnotationNode> annotationNodes) {
        for (AnnotationNode annotationNode : annotationNodes) {
            if (annotationNode.getClassNode().getNameWithoutPackage().equals("WithPlugin")) {
                Expression expression = annotationNode.getMember("value");

                Set<String> plugins = parseContextHolder.get().getPlugins();

                if (expression instanceof ConstantExpression) {
                    plugins.add(((String) ((ConstantExpression) expression).getValue()));
                } else if (expression instanceof ArrayExpression || expression instanceof ListExpression) {
                    List<Expression> items;

                    if (expression instanceof ArrayExpression) {
                        items = ((ArrayExpression) expression).getExpressions();
                    } else {
                        items = ((ListExpression) expression).getExpressions();
                    }

                    for (Expression itemExpression : items) {
                        if (itemExpression instanceof ConstantExpression) {
                            plugins.add(getStringConstant((ConstantExpression) itemExpression));
                        }
                    }
                }
            }
        }
    }

    private String getStringConstant(ConstantExpression expression) {
        return (String) expression.getValue();
    }
}
