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

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class DelegationClassLoader extends ClassLoader {

    private static final long DEFAULT_TIMEOUT = 30000L;
    private final BundleContext bundleContext;
    private String delegationContext;
    private long timeout = DEFAULT_TIMEOUT;

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String filterString = String.format("(%s=%s)", Constants.PROVIDED_CLASSES_KEY, name);
        if (delegationContext != null) {
            filterString = String.format("(&%s(%s=%s))",
                filterString,
                Constants.DELEGATION_CONTEXT_KEY, delegationContext);
        }
        Filter filter;
        try {
            filter = FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            throw new ClassNotFoundException(name, e);
        }
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, filter, null);
        serviceTracker.open();
        try {
            return doFindClass(name, serviceTracker);
        } finally {
            serviceTracker.close();
        }
    }

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

    public DelegationClassLoader(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public DelegationClassLoader(BundleContext bundleContext, ClassLoader parent) {
        super(parent);
        this.bundleContext = bundleContext;
    }

    public DelegationClassLoader(BundleContext bundleContext, String delegationContext) {
        this.bundleContext = bundleContext;
    }

    public DelegationClassLoader(BundleContext bundleContext, String delegationContext, ClassLoader parent) {
        super(parent);
        this.bundleContext = bundleContext;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
