package org.openengsb.labs.delegation.service.internal;

import java.util.Collection;
import java.util.Map;

import org.osgi.framework.Bundle;

public class ClassProviderWithAliases extends ClassProviderImpl {

    private Map<String, String> aliases;

    public ClassProviderWithAliases(Bundle bundle, Collection<String> classes, Map<String, String> aliases) {
        super(bundle, classes);
        this.aliases = aliases;
    }

    public ClassProviderWithAliases(Bundle bundle, String[] classes, Map<String, String> aliases) {
        super(bundle, classes);
        this.aliases = aliases;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (aliases.containsKey(name)) {
            return super.loadClass(aliases.get(name));
        }
        return super.loadClass(name);
    }

}
