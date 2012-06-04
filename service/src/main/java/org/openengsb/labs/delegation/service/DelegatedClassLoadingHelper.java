/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.labs.delegation.service;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class DelegatedClassLoadingHelper {
    private static final long DEFAULT_TIMEOUT = 30000L;
    private BundleContext bundleContext;
    private long timeout = DEFAULT_TIMEOUT;

    public DelegatedClassLoadingHelper(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Searches the OSGi environment for bundles which provide a class with the given name through the labs delegation
     * project. If there is such a bundle, the class object will be returned.
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, null, null);
    }

    /**
     * Searches the OSGi environment for bundles which provide a class with the given name and the given context through
     * the labs delegation project. If there is such a bundle, the class object will be returned.
     */
    public Class<?> loadClassWithContext(String name, String context) throws ClassNotFoundException {
        return loadClass(name, context, null);
    }

    /**
     * Searches the OSGi environment for bundles which provide a class with the given name and the given version through
     * the labs delegation project. If there is such a bundle, the class object will be returned.
     */
    public Class<?> loadClassWithVersion(String name, String version) throws ClassNotFoundException {
        return loadClass(name, null, version);
    }

    /**
     * Searches the OSGi environment for bundles which provide a class with the given name, the given context and the
     * given version through the labs delegation project. If there is such a bundle, the class object will be returned.
     */
    public Class<?> loadClass(String name, String context, String version) throws ClassNotFoundException {
        Filter filter = createFilter(name, context, version);
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, filter, null);
        serviceTracker.open();
        try {
            return doFindClass(name, serviceTracker);
        } finally {
            serviceTracker.close();
        }
    }

    /**
     * Creates the OSGi service filter based on the three given values. Skips null values except for the name value.
     */
    private Filter createFilter(String name, String context, String version) {
        if (name == null) {
            throw new IllegalArgumentException("Property name may not be null");
        }
        List<String> filterElements = new ArrayList<String>();
        filterElements.add(String.format("(%s=%s)", Constants.PROVIDED_CLASSES_KEY, name));
        if (context != null) {
            filterElements.add(String.format("(%s=%s)", Constants.DELEGATION_CONTEXT_KEY, context));
        }
        if (version != null) {
            filterElements.add(String.format("(%s=%s)", Constants.CLASS_VERSION_KEY, version));
        }
        StringBuilder filterString = new StringBuilder();
        for (String element : filterElements) {
            filterString.append(element);
        }
        if (filterElements.size() > 1) {
            filterString.insert(0, "(&");
            filterString.append(")");
        }
        try {
            return FrameworkUtil.createFilter(filterString.toString());
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter string: " + filterString.toString(), e);
        }
    }
    
    /**
     * Does the actual loading of the class object.
     */
    private Class<?> doFindClass(String name, ServiceTracker serviceTracker) throws ClassNotFoundException {
        ClassProvider service;
        try {
            service = (ClassProvider) serviceTracker.waitForService(timeout);
        } catch (InterruptedException e) {
            throw new ClassNotFoundException(name, e);
        }
        if (service == null) {
            throw new ClassNotFoundException(name);
        }
        return service.loadClass(name);
    }
}
