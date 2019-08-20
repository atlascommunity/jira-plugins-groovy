package ru.mail.jira.plugins.groovy.impl.groovy;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;


public class PackageCustomizer extends CompilationCustomizer {
    public PackageCustomizer() {
        super(CompilePhase.CONVERSION);
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        if (!classNode.hasPackageName()) {
            String className = classNode.getName();
            classNode.setName("mygroovy.scripts." + className);
        }
    }
}
