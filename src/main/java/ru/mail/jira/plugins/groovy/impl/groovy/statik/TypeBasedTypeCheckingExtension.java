package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContextHolder;

import java.util.List;
import java.util.Map;

public class TypeBasedTypeCheckingExtension extends AbstractTypeCheckingExtension {
    private final ScriptService scriptService;
    private final ParseContextHolder parseContextHolder;

    public TypeBasedTypeCheckingExtension(
        StaticTypeCheckingVisitor typeCheckingVisitor,
        ScriptService scriptService, ParseContextHolder parseContextHolder
    ) {
        super(typeCheckingVisitor);
        this.scriptService = scriptService;
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    public boolean handleUnresolvedVariableExpression(VariableExpression vexp) {
        //todo: handle PluginModule/StandardModule injections

        Map<String, Class> globalTypes = scriptService.getGlobalVariableTypes();
        Class type = globalTypes.get(vexp.getName());

        if (type == null) {
            Map<String, Class> types = parseContextHolder.get().getTypes();

            if (types != null) {
                type = types.get(vexp.getName());
            }
        }

        if (type != null) {
            storeType(vexp, ClassHelper.make(type));
            setHandled(true);
            return true;
        }

        return false;
    }

    @Override
    public void afterMethodCall(MethodCall call) {
        if (!parseContextHolder.get().isExtended()) {
            return;
        }

        if (call instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) call;
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
    }

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
        List warnings = parseContextHolder.get().getWarnings();

        warnings.add(new WarningMessage(
            warning,
            expression.getLineNumber(), expression.getColumnNumber(),
            expression.getLastLineNumber(), expression.getLastColumnNumber()
        ));
    }
}
