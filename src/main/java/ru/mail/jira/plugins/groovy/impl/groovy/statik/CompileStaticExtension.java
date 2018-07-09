package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.transform.ASTTransformation;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContextHolder;

public class CompileStaticExtension extends CompilationCustomizer {
    private final AnnotationNode annotationNode = new AnnotationNode(new ClassNode(CompileStatic.class));
    private final ASTTransformation transformation;
    private final ParseContextHolder parseContextHolder;

    public CompileStaticExtension(ParseContextHolder parseContextHolder, ScriptService scriptService) {
        super(CompilePhase.INSTRUCTION_SELECTION);
        this.transformation = new ExtendedStaticCompileTransformation(parseContextHolder, scriptService);
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        if (parseContextHolder.get().isCompileStatic()) {
            transformation.visit(new ASTNode[] {annotationNode, classNode}, source);
        }
    }
}
