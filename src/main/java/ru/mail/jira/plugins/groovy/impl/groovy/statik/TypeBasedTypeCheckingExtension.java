package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import ru.mail.jira.plugins.groovy.api.service.ScriptService;
import ru.mail.jira.plugins.groovy.impl.groovy.ParseContextHolder;

import java.util.Map;

public class TypeBasedTypeCheckingExtension extends AbstractTypeCheckingExtension {
    private final ScriptService scriptService;
    private final ParseContextHolder parseContextHolder;

    public TypeBasedTypeCheckingExtension(
        StaticTypeCheckingVisitor typeCheckingVisitor,
        ScriptService scriptService, ParseContextHolder parseContextHolder
    ) {
        super(typeCheckingVisitor);
        this.scriptService = scriptService;
        this.parseContextHolder = parseContextHolder;
    }

    @Override
    public boolean handleUnresolvedVariableExpression(VariableExpression vexp) {
        Map<String, Class> globalTypes = scriptService.getGlobalVariableTypes();
        Class type = globalTypes.get(vexp.getName());

        if (type == null) {
            Map<String, Class> types = parseContextHolder.get().getTypes();

            if (types != null) {
                type = types.get(vexp.getName());
            }
        }

        if (type != null) {
            storeType(vexp, ClassHelper.make(type));
            setHandled(true);
            return true;
        }

        return false;
    }
}
