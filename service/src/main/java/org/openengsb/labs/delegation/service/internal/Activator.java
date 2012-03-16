package org.openengsb.labs.delegation.service.internal;

import java.util.Hashtable;

import org.openengsb.labs.delegation.api.ClassProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    
    public void start(BundleContext context) {
        BundleListener bundleListener = new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                if (event.getType() == BundleEvent.STARTED) {
                    handleBundleInstall(event.getBundle());
                }
            }
        };
        context.addBundleListener(bundleListener);
        for (Bundle b : context.getBundles()) {
            if (b.getState() == Bundle.ACTIVE) {
                handleBundleInstall(b);
            }
        }
    }

    private synchronized void handleBundleInstall(Bundle b) {
        LOGGER.info("injecting ClassProvider-Service into bundle {}.", b.getSymbolicName());
        ClassloadingDelegateImpl service = new ClassloadingDelegateImpl(b);
        b.getBundleContext().registerService(ClassProvider.class.getName(), service, new Hashtable<String, Object>());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
