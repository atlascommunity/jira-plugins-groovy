package ru.mail.jira.plugins.groovy.impl.groovy.var;

public class GlobalObjectClassLoader extends ClassLoader {
    private final GlobalObjectsBindingProvider globalObjectsBindingProvider;

    public GlobalObjectClassLoader(GlobalObjectsBindingProvider globalObjectsBindingProvider) {
        this.globalObjectsBindingProvider = globalObjectsBindingProvider;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = globalObjectsBindingProvider.getTypes().get(name);

        if (result == null) {
            throw new ClassNotFoundException();
        }

        return result;
    }
}
