package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import com.atlassian.annotations.Internal;
import lombok.Getter;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeprecatedAstVisitor extends ClassCodeVisitorSupport {
    @Getter
    private final List<WarningMessage> warnings = new ArrayList<>();

    @Override
    public void visitClassExpression(ClassExpression expression) {
        super.visitClassExpression(expression);

        ClassNode inferredType = expression.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);

        if (inferredType.getTypeClass() == Class.class && inferredType.getGenericsTypes().length > 0) {
            inferredType = inferredType.getGenericsTypes()[0].getType();
        } else {
            return;
        }

        if (inferredType != null) {
            if (hasAnnotation(inferredType, Deprecated.class)) {
                addWarning(expression, "Deprecated class: " + inferredType.getName());
            }
            if (hasAnnotation(inferredType, Internal.class)) {
                addWarning(expression, "Internal class: " + inferredType.getName());
            }
        }
    }

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

            if (hasAnnotation(methodCall, Deprecated.class)) {
                addWarning(methodCallExpression, "Deprecated method: " + buildMethodString(methodCall));
            }
            if (hasAnnotation(methodCall, Internal.class)) {
                addWarning(methodCallExpression, "Internal method: " + buildMethodString(methodCall));
            }
        }
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        super.visitPropertyExpression(expression);

        if (expression.hasNoRealSourcePosition()) {
            return;
        }

        Expression objectExpression = expression.getObjectExpression();
        Expression propertyExpression = expression.getProperty();

        if (propertyExpression instanceof ConstantExpression) {
            ClassNode classNode = objectExpression.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);

            if (classNode != null) {
                ConstantExpression propertyConstant = (ConstantExpression) propertyExpression;

                if (propertyConstant.getValue() instanceof String) {
                    String constant = (String) propertyConstant.getValue();

                    PropertyNode property = classNode.getProperty(constant);
                    FieldNode field = classNode.getField(constant);
                    if (field != null) {
                        if (hasAnnotation(field, Deprecated.class)) {
                            addWarning(expression, "Deprecated field: " + buildFieldString(field));
                        }
                        if (hasAnnotation(field, Internal.class)) {
                            addWarning(expression, "Internal field: " + buildFieldString(field));
                        }

                        return;
                    }

                    MethodNode methodNode = classNode.getGetterMethod("get" + MetaClassHelper.capitalize(constant));

                    if (methodNode != null) {
                        if (hasAnnotation(methodNode, Deprecated.class)) {
                            addWarning(expression, "Deprecated method: " + buildMethodString(methodNode));
                        }
                        if (hasAnnotation(methodNode, Internal.class)) {
                            addWarning(expression, "Internal method: " + buildMethodString(methodNode));
                        }
                    }
                }
            }
        }
    }

    private boolean hasAnnotation(AnnotatedNode node, Class<?> annotationClass) {
        for (AnnotationNode annotationNode : node.getAnnotations()) {
            ClassNode annotationClassNode = annotationNode.getClassNode();
            if (annotationClassNode.isResolved()) {
                if (annotationClass == annotationClassNode.getTypeClass()) {
                    return true;
                }
            }
        }

        return false;
    }

    private String buildMethodString(MethodNode methodNode) {
        StringBuilder sb = new StringBuilder();

        sb.append(methodNode.getDeclaringClass().getName()).append('.').append(methodNode.getName()).append('(');
        boolean isFirst = true;
        for (Parameter parameter : methodNode.getParameters()) {
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

    private String buildFieldString(FieldNode fieldNode) {
        return fieldNode.getDeclaringClass().getName() + "." + fieldNode.getName();
    }

    private void addWarning(AnnotatedNode expression, String warning) {
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
