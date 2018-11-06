package ru.mail.jira.plugins.groovy.api.script.binding;

import io.atlassian.fugue.Suppliers;
import ru.mail.jira.plugins.groovy.api.dto.docs.ClassDoc;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class LazyDocBindingDescriptorImpl<T> extends BindingDescriptorImpl<T> {
    private final Supplier<ClassDoc> docSupplier;

    public LazyDocBindingDescriptorImpl(T object, Class<T> type, Supplier<ClassDoc> docSupplier) {
        super(object, type);

        this.docSupplier = Suppliers.memoize(docSupplier);
    }

    @Nonnull
    @Override
    public ClassDoc getDoc() {
        return docSupplier.get();
    }
}
