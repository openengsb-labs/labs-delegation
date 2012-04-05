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

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import org.openengsb.labs.delegation.service.internal.ClassloadingDelegateImpl;
import org.osgi.framework.Bundle;

public final class DelegationUtil {

    /**
     * registers a ClassProvider service that is able to load all classes contained in the given bundle
     */
    public static ClassProvider registerClassProviderForBundle(Bundle b) {
        return doRegisterClassProviderForBundle(b, discoverClasses(b));
    }

    /**
     * registers a ClassProvider service that is able to load all classes contained in the given bundle that match any
     * of the given filters.
     * 
     * The filters is a list of packages. The list may use wildcards.
     * 
     * Example: my.bundle.mainpackage, my.bundle.otherpackage.*
     */
    public static ClassProvider registerClassProviderForBundle(Bundle b, Collection<String> classFilter) {
        Collection<String> discoveredClasses = discoverClasses(b);
        Set<String> matchingClasses = getMatchingClasses(classFilter, discoveredClasses);
        return doRegisterClassProviderForBundle(b, matchingClasses);
    }

    private static ClassProvider doRegisterClassProviderForBundle(Bundle b, Set<String> classes) {
        ClassProvider service = new ClassloadingDelegateImpl(b, classes);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.PROVIDED_CLASSES_KEY, classes);
        b.getBundleContext().registerService(ClassProvider.class.getName(), service, properties);
        return service;
    }

    private static Set<String> getMatchingClasses(Collection<String> classFilters, Collection<String> allClasses) {
        Set<String> matchingClasses = new HashSet<String>();
        Collection<String> expressions = prepareFilterExpressions(classFilters);
        for (String classname : allClasses) {
            for (String e : expressions) {
                if (Pattern.matches(e, classname)) {
                    matchingClasses.add(classname);
                    break;
                }
            }
        }
        return matchingClasses;
    }

    private static Collection<String> prepareFilterExpressions(Collection<String> classFilters) {
        Collection<String> expressions = new LinkedList<String>();
        for (String cFilter : classFilters) {
            expressions.add(
                cFilter
                    .replaceAll("\\.", "\\.")
                    .replaceAll("\\*", ".*")
                );
        }
        return expressions;
    }

    private static Set<String> discoverClasses(Bundle bundle) {
        @SuppressWarnings("unchecked")
        Enumeration<URL> classEntries = bundle.findEntries("/", "*.class", true);
        Set<String> discoveredClasses = new HashSet<String>();
        while (classEntries.hasMoreElements()) {
            URL classURL = classEntries.nextElement();
            String className = extractClassName(classURL);
            discoveredClasses.add(className);
        }
        return discoveredClasses;
    }

    private static String extractClassName(URL classURL) {
        String path = classURL.getPath();
        return path
            .replaceAll("^/", "")
            .replaceAll(".class$", "")
            .replaceAll("\\/", ".");
    }

    private DelegationUtil() {
    }

}
