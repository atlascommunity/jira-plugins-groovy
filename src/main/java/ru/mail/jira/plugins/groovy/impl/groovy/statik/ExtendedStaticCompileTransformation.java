package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.transform.sc.StaticCompileTransformation;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContextHolder;

public class ExtendedStaticCompileTransformation extends StaticCompileTransformation {
    private final ParseContextHolder parseContextHolder;
    private final ScriptService scriptService;

    public ExtendedStaticCompileTransformation(ParseContextHolder parseContextHolder, ScriptService scriptService) {
        this.parseContextHolder = parseContextHolder;
        this.scriptService = scriptService;
    }

    @Override
    protected void addTypeCheckingExtensions(StaticTypeCheckingVisitor visitor, Expression extensions) {
        visitor.addTypeCheckingExtension(new TypeBasedTypeCheckingExtension(visitor, scriptService, parseContextHolder));
    }
}