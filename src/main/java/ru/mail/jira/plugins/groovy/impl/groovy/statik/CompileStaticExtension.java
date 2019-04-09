package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import groovy.transform.CompilationUnitAware;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContextHolder;

public class CompileStaticExtension extends CompilationCustomizer implements CompilationUnitAware {
    private final AnnotationNode annotationNode = new AnnotationNode(new ClassNode(CompileStatic.class));
    private final ThreadLocal<CompilationUnit> currentCompilationUnit = new ThreadLocal<>();
    private final ParseContextHolder parseContextHolder;
    private final ScriptService scriptService;

    public CompileStaticExtension(ParseContextHolder parseContextHolder, ScriptService scriptService) {
        super(CompilePhase.INSTRUCTION_SELECTION);
        this.parseContextHolder = parseContextHolder;
        this.scriptService = scriptService;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        try {
            if (parseContextHolder.get().isCompileStatic()) {
                ExtendedStaticCompileTransformation transformation = new ExtendedStaticCompileTransformation(parseContextHolder, scriptService);
                if (currentCompilationUnit.get() != null) {
                    transformation.setCompilationUnit(currentCompilationUnit.get());
                }
                transformation.visit(new ASTNode[]{annotationNode, classNode}, source);
            }
        } finally {
            currentCompilationUnit.remove();
        }
    }

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        currentCompilationUnit.set(unit);
    }
}
