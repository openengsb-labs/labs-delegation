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

package org.openengsb.labs.delegation.itests.bundles.consumer.internal;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConsumer implements BundleActivator {

    public static class ResultTask implements Callable<Method> {
        private Method result;

        public ResultTask(Method result) {
            this.result = result;
        }

        @Override
        public Method call() throws Exception {
            return result;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TestConsumer.class);

    @Override
    public void start(BundleContext context) throws Exception {
        Bundle[] bundles = context.getBundles();
        for (Bundle b : bundles) {
            if (b.getSymbolicName().startsWith("test.provider")) {
                DelegationUtil.registerClassProviderForBundle(b);
            }
        }

        ServiceReference[] allServiceReferences = context.getAllServiceReferences(ClassProvider.class.getName(), null);
        if (allServiceReferences == null) {
            System.err.println("args, no refs");
        }
        for (ServiceReference r : allServiceReferences) {
            ClassProvider service = (ClassProvider) context.getService(r);
            Class<?> cls;
            try {
                cls = service.loadClass("org.openengsb.labs.delegation.itests.bundles.provider.TestService");
            } catch (Exception e) {
                LOGGER.info("cnfe", e);
                continue;
            }
            ResultTask resultTask = new ResultTask(cls.getMethod("doSomething"));
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("resultProvider", "test");
            context.registerService(Callable.class.getName(), resultTask, properties);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
