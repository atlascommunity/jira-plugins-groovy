package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import lombok.Getter;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeprecatedAstVisitor extends ClassCodeVisitorSupport {
    @Getter
    private final List<WarningMessage> warnings = new ArrayList<>();

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCallExpression) {
        super.visitMethodCallExpression(methodCallExpression);

        if (methodCallExpression.hasNoRealSourcePosition()) {
            return;
        }

        Map<?, ?> metaData = methodCallExpression.getNodeMetaData();

        Object rawMethodCall = metaData.get(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);

        if (rawMethodCall instanceof MethodNode) {
            MethodNode methodCall = (MethodNode) rawMethodCall;

            if (hasDeprecatedAnnotation(methodCall)) {
                addWarning(methodCallExpression, "Deprecated method: " + buildMethodString(methodCall));
                return;
            }

            ClassNode declaringClass = methodCall.getDeclaringClass();
            if (hasDeprecatedAnnotation(declaringClass)) {
                addWarning(methodCallExpression, "Deprecated class: " + declaringClass.getName());
            }
        }
    }

    /*
    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        super.visitPropertyExpression(expression);

        if (expression.hasNoRealSourcePosition()) {
            return;
        }

        Expression objectExpression = expression.getObjectExpression();
        Expression propertyExpression = expression.getProperty();

        if (objectExpression instanceof ClassExpression && propertyExpression instanceof ConstantExpression) {
            ClassExpression classExpression = (ClassExpression) objectExpression;
            ClassNode classNode = classExpression.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);

            if (classNode != null) {
                ConstantExpression propertyConstant = (ConstantExpression) propertyExpression;

                PropertyNode property = classNode.getProperty((String) propertyConstant.getValue());
                MethodNode methodNode = classNode.getGetterMethod((String) propertyConstant.getValue());

                System.out.println(property);
            }
        }
        //System.out.println(expression);
    }*/

    private boolean hasDeprecatedAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotationNode : node.getAnnotations()) {
            ClassNode annotationClassNode = annotationNode.getClassNode();
            if (annotationClassNode.isResolved()) {
                if (Deprecated.class == annotationClassNode.getTypeClass()) {
                    return true;
                }
            }
        }

        return false;
    }

    private String buildMethodString(MethodNode methodCall) {
        StringBuilder sb = new StringBuilder();

        sb.append(methodCall.getDeclaringClass().getName()).append('.').append(methodCall.getName()).append('(');
        boolean isFirst = true;
        for (Parameter parameter : methodCall.getParameters()) {
            if (!isFirst) {
                sb.append(',');
            } else {
                isFirst = false;
            }
            sb.append(parameter.getType().getName());
        }
        sb.append(')');

        return sb.toString();
    }

    private void addWarning(MethodCallExpression expression, String warning) {
        warnings.add(new WarningMessage(
            warning,
            expression.getLineNumber(), expression.getColumnNumber(),
            expression.getLastLineNumber(), expression.getLastColumnNumber()
        ));
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }


}
