package ru.mail.jira.plugins.groovy.impl.groovy;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import ru.mail.jira.plugins.groovy.api.script.ParseContext;

public class InjectionExtension extends CompilationCustomizer {
    private final ParseContextHolder parseContextHolder;
    private final InjectionVisitor injectionVisitor;

    public InjectionExtension(ParseContextHolder parseContextHolder) {
        super(CompilePhase.CANONICALIZATION);
        this.parseContextHolder = parseContextHolder;
        this.injectionVisitor = new InjectionVisitor(parseContextHolder);
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        ParseContext parseContext = parseContextHolder.get();

        if (parseContext.getCompletedExtensions().contains(InjectionExtension.class)) {
            return;
        }

        source.getAST().getClasses().forEach(injectionVisitor::visitClass);
        parseContext.getCompletedExtensions().add(InjectionExtension.class);
    }


}
