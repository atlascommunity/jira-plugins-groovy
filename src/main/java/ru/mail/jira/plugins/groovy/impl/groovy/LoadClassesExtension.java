package ru.mail.jira.plugins.groovy.impl.groovy;

import com.atlassian.plugin.Plugin;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import ru.mail.jira.plugins.groovy.api.service.InjectionResolver;
import ru.mail.jira.plugins.groovy.util.DelegatingClassLoader;

import java.util.stream.Collectors;

public class LoadClassesExtension extends CompilationCustomizer {
    private final ParseContextHolder parseContextHolder;
    private final InjectionResolver injectionResolver;
    private final DelegatingClassLoader classLoader;

    public LoadClassesExtension(ParseContextHolder parseContextHolder, InjectionResolver injectionResolver, DelegatingClassLoader classLoader) {
        super(CompilePhase.CONVERSION);
        this.parseContextHolder = parseContextHolder;
        this.injectionResolver = injectionResolver;
        this.classLoader = classLoader;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        ParseContext parseContext = parseContextHolder.get();
        if (parseContext.getCompletedExtensions().contains(LoadClassesExtension.class)) {
            return;
        }

        //make sure we load all plugins before class resolution happens
        classLoader.ensureAvailability(
            parseContext
                .getPlugins()
                .stream()
                .map(key -> {
                    Plugin plugin = injectionResolver.getPlugin(key);
                    if (plugin == null) {
                        throw new RuntimeException("Unable to load plugin " + key);
                    }

                    return plugin;
                })
                .collect(Collectors.toSet())
        );

        parseContext.getCompletedExtensions().add(LoadClassesExtension.class);
    }
}
