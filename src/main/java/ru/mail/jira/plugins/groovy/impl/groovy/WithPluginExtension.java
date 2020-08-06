package ru.mail.jira.plugins.groovy.impl.groovy;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import ru.mail.jira.plugins.groovy.api.script.ParseContext;

public class WithPluginExtension extends CompilationCustomizer {
    private final ParseContextHolder parseContextHolder;
    private final WithPluginVisitor visitor;

    public WithPluginExtension(ParseContextHolder parseContextHolder) {
        super(CompilePhase.CONVERSION);
        this.parseContextHolder = parseContextHolder;
        this.visitor = new WithPluginVisitor(parseContextHolder);
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode ignore) throws CompilationFailedException {
        ParseContext parseContext = parseContextHolder.get();
        if (parseContext.getCompletedExtensions().contains(WithPluginExtension.class)) {
            return;
        }

        source.getAST().getClasses().forEach(visitor::visitClass);

        parseContext.getCompletedExtensions().add(WithPluginExtension.class);
    }
}
