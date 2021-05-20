package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

public class AllVariablesMemorizerExtension extends CompilationCustomizer {
    private final ClassCodeVisitorSupport visitor;

    public AllVariablesMemorizerExtension(ClassCodeVisitorSupport visitor) {
        super(CompilePhase.CANONICALIZATION);
        this.visitor = visitor;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        classNode.visitContents(visitor);
    }
}