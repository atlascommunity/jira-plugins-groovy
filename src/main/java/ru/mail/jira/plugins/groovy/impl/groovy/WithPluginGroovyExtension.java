package ru.mail.jira.plugins.groovy.impl.groovy;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import ru.mail.jira.plugins.groovy.api.script.ParseContext;

import java.util.List;
import java.util.Set;

public class WithPluginGroovyExtension extends CompilationCustomizer {
    private final ParseContextHolder parseContextHolder;

    public WithPluginGroovyExtension(ParseContextHolder parseContextHolder) {
        super(CompilePhase.CONVERSION);
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode ignore) throws CompilationFailedException {
        ParseContext parseContext = parseContextHolder.get();
        if (parseContext.getCompletedExtensions().contains(WithPluginGroovyExtension.class)) {
            return;
        }

        for (Statement statement : source.getAST().getStatementBlock().getStatements()) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement castedStatement = (ExpressionStatement) statement;
                processAnnotations(parseContext, castedStatement.getExpression().getAnnotations());
            }
        }

        source.getAST().getClasses().forEach(classNode -> processAnnotations(parseContext, classNode.getAnnotations()));

        parseContext.getCompletedExtensions().add(WithPluginGroovyExtension.class);
    }

    private void processAnnotations(ParseContext parseContext, List<AnnotationNode> annotationNodes) {
        for (AnnotationNode annotationNode : annotationNodes) {
            if (annotationNode.getClassNode().getNameWithoutPackage().equals("WithPlugin")) {
                Expression expression = annotationNode.getMember("value");

                Set<String> plugins = parseContext.getPlugins();

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
