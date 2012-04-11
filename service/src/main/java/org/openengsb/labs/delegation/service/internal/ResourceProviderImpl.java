package org.openengsb.labs.delegation.service.internal;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openengsb.labs.delegation.service.ResourceProvider;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceProviderImpl implements ResourceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProviderImpl.class);

    private Bundle bundle;
    private Set<String> resources;
    private Collection<URL> allResources;

    public ResourceProviderImpl(Bundle bundle, Set<String> resources) {

        this.bundle = bundle;
        this.resources = resources;
    }

    @Override
    public URL loadResource(String name) {
        LOGGER.debug("loading ressource {} by delegation", name);
        if (resources.contains(name)) {
            return bundle.getResource(name);
        }
        return null;
    }

    @Override
    public Collection<URL> listResources() {
        if(allResources == null){
            loadAllResources();
        }
        return new HashSet<URL>(allResources);
    }

    private void loadAllResources() {
        allResources = new HashSet<URL>();
        for(String s : resources){
            URL resource = bundle.getResource(s);
            allResources.add(resource);
        }
    }

}
