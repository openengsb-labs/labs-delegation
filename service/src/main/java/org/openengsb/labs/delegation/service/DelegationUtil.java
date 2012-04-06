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

import java.util.Collection;

import org.openengsb.labs.delegation.service.internal.BundleHandler;
import org.osgi.framework.Bundle;

public final class DelegationUtil {

    /**
     * registers a ClassProvider service that is able to load all classes contained in the given bundle
     */
    public static void registerClassProviderForBundle(Bundle bundle) {
        BundleHandler.injectIntoBundle(bundle);
    }

    /**
     * registers a ClassProvider service with the given delegationContext that is able to load all classes contained in
     * the given bundle
     */
    public static void registerClassProviderForBundle(Bundle bundle, String delegationContext) {
        BundleHandler.injectIntoBundle(bundle, delegationContext);
    }

    /**
     * registers a ClassProvider service that is able to load all classes contained in the given bundle that match any
     * of the given filters.
     * 
     * The filters is a list of packages. The list may use wildcards.
     * 
     * Example: my.bundle.mainpackage, my.bundle.otherpackage.*
     */
    public static void registerClassProviderForBundle(Bundle bundle, Collection<String> classFilters) {
        BundleHandler.injectIntoBundle(bundle, classFilters);
    }

    /**
     * registers a ClassProvider service with the given delegationContext that is able to load all classes contained in
     * the given bundle that match any of the given filters.
     * 
     * The filters is a list of packages. The list may use wildcards.
     * 
     * Example: my.bundle.mainpackage, my.bundle.otherpackage.*
     */
    public static void registerClassProviderForBundle(Bundle bundle, Collection<String> classFilters,
            String delegationContext) {
        BundleHandler.injectIntoBundle(bundle, classFilters, delegationContext);
    }

    /**
     * registers a ResourceProvider service that is able to load all resources matching any of the given filters.
     * 
     * The filters is a list of file-path patterns. The list may use wildcards.
     * 
     * Example: OSGI-INF/blueprint/context.xml, resources/*
     */
    public static void registerResourceProviderForBundle(Bundle bundle, Collection<String> fileFilters) {
        BundleHandler.injectResourceProviderIntoBundle(bundle, fileFilters);
    }

    /**
     * registers a ResourceProvider service with the given delegationContext that is able to load all resources matching any of the given filters.
     * 
     * The filters is a list of file-path patterns. The list may use wildcards.
     * 
     * Example: OSGI-INF/blueprint/context.xml, resources/*
     */
    public static void registerResourceProviderForBundle(Bundle bundle, Collection<String> fileFilters,
            String delegationContext) {
        BundleHandler.injectResourceProviderIntoBundle(bundle, fileFilters, delegationContext);
    }

    private DelegationUtil() {
    }

}
