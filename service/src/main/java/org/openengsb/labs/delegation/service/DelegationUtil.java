package org.openengsb.labs.delegation.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import org.openengsb.labs.delegation.api.ClassProvider;
import org.openengsb.labs.delegation.service.internal.ClassloadingDelegateImpl;
import org.osgi.framework.Bundle;

public final class DelegationUtil {

    public static ClassProvider registerClassProviderForBundle(Bundle b) {
        return registerClassProviderForBundle(b, Arrays.asList("*"));
    }

    public static ClassProvider registerClassProviderForBundle(Bundle b, Collection<String> classFilter) {
        ClassProvider service = new ClassloadingDelegateImpl(b, classFilter);
        b.getBundleContext().registerService(ClassProvider.class.getName(), service, new Hashtable<String, Object>());
        return service;
    }

    private DelegationUtil() {
    }

}
