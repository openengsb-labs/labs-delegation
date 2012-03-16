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

import java.util.Hashtable;

import org.openengsb.labs.delegation.api.ClassProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatedClassloaderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedClassloaderManager.class);

    private BundleContext bundleContext;

    public DelegatedClassloaderManager(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void start() {
        BundleListener bundleListener = new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                if (event.getType() == BundleEvent.STARTED) {
                    handleBundleInstall(event.getBundle());
                }
            }
        };
        bundleContext.addBundleListener(bundleListener);
        for (Bundle b : bundleContext.getBundles()) {
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
}
