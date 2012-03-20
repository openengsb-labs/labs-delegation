package org.openengsb.labs.delegation.service.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    @Override
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
        String providedClassesString = (String) b.getHeaders().get(Constants.PROVIDED_CLASSES);
        if (providedClassesString == null || providedClassesString.isEmpty()) {
            return;
        }
        Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
        DelegationUtil.registerClassProviderForBundle(b, providedClasses);
    }

    private Collection<String> parseProvidedClasses(String providedClassesString) {
        String[] providedClassesArray = providedClassesString.split(",");
        Collection<String> providedClassesList = new ArrayList<String>();
        for (String p : providedClassesArray) {
            providedClassesList.add(p.trim());
        }
        return providedClassesList;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
