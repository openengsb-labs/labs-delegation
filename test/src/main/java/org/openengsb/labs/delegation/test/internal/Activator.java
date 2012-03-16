package org.openengsb.labs.delegation.test.internal;

import org.openengsb.labs.delegation.api.ClassProvider;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        for (Bundle b : context.getBundles()) {
            if (b.getSymbolicName().startsWith("org.openengsb.labs")) {
                DelegationUtil.registerClassProviderForBundle(b);
            }
        }

        ServiceReference[] allServiceReferences = context.getAllServiceReferences(ClassProvider.class.getName(), null);
        if (allServiceReferences == null) {
            System.err.println("args, no refs");
        }
        for (ServiceReference r : allServiceReferences) {
            ClassProvider service = (ClassProvider) context.getService(r);
            Class<?> cls;
            try {
                cls = service.loadClass("org.openengsb.labs.delegation.service.internal.Activator");
            } catch (Exception e) {
                continue;
            }
            System.out.println(cls);
        }
        System.out.println("done");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
