package ru.mail.jira.plugins.groovy.impl.groovy.statik;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import ru.mail.jira.plugins.groovy.api.script.binding.BindingDescriptor;
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
        if (parseContextHolder.get().isSingleton()) {
            return false;
        }

        Map<String, BindingDescriptor> globalBindings = scriptService.getGlobalBindings();
        BindingDescriptor globalBinding = globalBindings.get(vexp.getName());

        Class type = null;

        if (globalBinding != null) {
            type = globalBinding.getType();
        } else {
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
