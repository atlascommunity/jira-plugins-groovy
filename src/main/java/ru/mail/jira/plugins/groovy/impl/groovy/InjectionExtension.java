package ru.mail.jira.plugins.groovy.impl.groovy;

import org.apache.felix.framework.BundleWiringImpl;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

public class InjectionExtension extends CompilationCustomizer {
    private final ParseContextHolder parseContextHolder;

    public InjectionExtension(ParseContextHolder parseContextHolder) {
        super(CompilePhase.CANONICALIZATION);
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        for (Statement statement : source.getAST().getStatementBlock().getStatements()) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement castedStatement = (ExpressionStatement) statement;
                if (castedStatement.getExpression() instanceof DeclarationExpression) {
                    DeclarationExpression expression = (DeclarationExpression) castedStatement.getExpression();

                    if (expression.getLeftExpression() instanceof VariableExpression) {
                        VariableExpression leftExpression = (VariableExpression) expression.getLeftExpression();

                        for (AnnotationNode annotationNode : expression.getAnnotations()) {
                            String annotationName = annotationNode.getClassNode().getName();
                            boolean pluginModule = annotationName.equals("ru.mail.jira.plugins.groovy.api.script.PluginModule");
                            boolean standardModule = annotationName.equals("ru.mail.jira.plugins.groovy.api.script.StandardModule");
                            if (pluginModule || standardModule) {
                                ClassNode type = leftExpression.getType();

                                ScriptInjection injectionObject;

                                String varName = leftExpression.getName();

                                if (pluginModule) {
                                    ClassLoader typeClassLoader = type.getTypeClass().getClassLoader();

                                    if (typeClassLoader instanceof BundleWiringImpl.BundleClassLoader) {
                                        BundleWiringImpl.BundleClassLoader castedClassLoader = (BundleWiringImpl.BundleClassLoader) typeClassLoader;

                                        injectionObject = new ScriptInjection(
                                            castedClassLoader.getBundle().getSymbolicName(),
                                            type.getName(),
                                            varName
                                        );
                                    } else {
                                        throw new RuntimeException("Class is not from osgi bundle");
                                    }
                                } else {
                                    injectionObject = new ScriptInjection(
                                        null,
                                        type.getName(),
                                        varName
                                    );
                                }

                                parseContextHolder.get().getInjections().add(injectionObject);
                                expression.setRightExpression(new VariableExpression(varName));

                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
