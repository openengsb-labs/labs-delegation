package org.openengsb.labs.delegation.service;

import java.net.URL;
import java.util.Collection;

public interface ResourceProvider {

    /**
     * loads the resource using the classloader of the bundle providing the service
     */
    URL loadResource(String name);

    /**
     * returns a list of all resources provided by this service
     */
    Collection<URL> listResources();
}
