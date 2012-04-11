package org.openengsb.labs.delegation.service.internal;

import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;

public class ClassProviderWithAliases extends ClassProviderImpl {

    private Map<String, String> aliases;

    public ClassProviderWithAliases(Bundle bundle, Set<String> classes, Map<String, String> aliases) {
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
