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

package org.openengsb.labs.delegation.service.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.openengsb.labs.delegation.service.Provide;
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
        @SuppressWarnings("unchecked")
        Enumeration<String> keys = b.getHeaders().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.equals(Constants.PROVIDED_CLASSES)) {
                String providedClassesString = (String) b.getHeaders().get(key);
                Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
                DelegationUtil.registerClassProviderForBundle(b, providedClasses);
            } else if (key.startsWith(Constants.PROVIDED_CLASSES)) {
                String context = key.replaceFirst(Constants.PROVIDED_CLASSES + "\\-", "");
                String providedClassesString = (String) b.getHeaders().get(key);
                Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
                DelegationUtil.registerClassProviderForBundle(b, providedClasses, context);
            } else if (key.equals(Constants.DELEGATION_ANNOTATIONS)) {
                Set<String> discoverClasses = discoverClasses(b);
                Map<String, Set<String>> providedClasses = new HashMap<String, Set<String>>();
                for (String classname : discoverClasses) {
                    Class<?> clazz;
                    try {
                        clazz = b.loadClass(classname);
                    } catch (ClassNotFoundException e) {
                        LOGGER.warn("bundle could not find own class: " + classname, e);
                        continue;

                    }
                    Provide provide = clazz.getAnnotation(Provide.class);
                    if (provide != null) {
                        getOrCreate(providedClasses, "").add(classname);
                        for (String context : provide.value()) {
                            getOrCreate(providedClasses, context).add(classname);
                        }
                    }
                }
                for (Map.Entry<String, Set<String>> entry : providedClasses.entrySet()) {
                    ClassProvider service = new ClassloadingDelegateImpl(b, entry.getValue());
                    Hashtable<String, Object> properties = new Hashtable<String, Object>();
                    properties.put(Constants.PROVIDED_CLASSES_KEY, entry.getValue());
                    properties.put(Constants.DELEGATION_CONTEXT, entry.getKey());
                    b.getBundleContext().registerService(ClassProvider.class.getName(), service, properties);
                }
            }
        }
    }

    private static <K> Set<String> getOrCreate(Map<K, Set<String>> map, K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        Set<String> set = new HashSet<String>();
        map.put(key, set);
        return set;
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

    }

    public static Set<String> discoverClasses(Bundle bundle) {
        @SuppressWarnings("unchecked")
        Enumeration<URL> classEntries = bundle.findEntries("/", "*.class", true);
        Set<String> discoveredClasses = new HashSet<String>();
        while (classEntries.hasMoreElements()) {
            URL classURL = classEntries.nextElement();
            String className = DelegationUtil.extractClassName(classURL);
            discoveredClasses.add(className);
        }
        return discoveredClasses;
    }

}
