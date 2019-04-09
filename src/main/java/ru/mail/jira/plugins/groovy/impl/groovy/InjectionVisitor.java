package ru.mail.jira.plugins.groovy.impl.groovy;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import ru.mail.jira.plugins.groovy.api.script.ScriptInjection;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import java.util.Optional;

public class InjectionVisitor extends ClassCodeVisitorSupport {
    private final ParseContextHolder parseContextHolder;

    public InjectionVisitor(ParseContextHolder parseContextHolder) {
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    @Override
    public void visitField(FieldNode node) {
        super.visitField(node);

        getInitialExpressionIfApplicable(node, node.getType(), node.getName())
            .ifPresent(node::setInitialValueExpression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        super.visitDeclarationExpression(expression);

        if (expression.getLeftExpression() instanceof VariableExpression) {
            VariableExpression leftExpression = (VariableExpression) expression.getLeftExpression();

            getInitialExpressionIfApplicable(expression, leftExpression.getType(), leftExpression.getName())
                .ifPresent(expression::setRightExpression);
        }
    }

    private Optional<Expression> getInitialExpressionIfApplicable(AnnotatedNode node, ClassNode type, String varName) {
        for (AnnotationNode annotationNode : node.getAnnotations()) {
            String annotationName = annotationNode.getClassNode().getName();
            boolean pluginModule = annotationName.equals("ru.mail.jira.plugins.groovy.api.script.PluginModule");
            boolean standardModule = annotationName.equals("ru.mail.jira.plugins.groovy.api.script.StandardModule");
            if (pluginModule || standardModule) {
                ScriptInjection injectionObject;

                if (pluginModule) {
                    injectionObject = new ScriptInjection(
                        ClassLoaderUtil.getClassBundleName(type.getTypeClass()),
                        type.getName(),
                        varName
                    );
                } else {
                    injectionObject = new ScriptInjection(
                        null,
                        type.getName(),
                        varName
                    );
                }

                parseContextHolder.get().getInjections().add(injectionObject);

                return Optional.of(buildInitialExpression(varName));
            }
        }

        return Optional.empty();
    }

    private Expression buildInitialExpression(String varName) {
        return new MethodCallExpression(
            new PropertyExpression(new VariableExpression("this"), "binding"),
            "getVariable",
            new ArgumentListExpression(new ConstantExpression(varName))
        );
    }
}
