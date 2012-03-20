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

package org.openengsb.labs.delegation.itests.bundles.provider.internal;

import java.util.Hashtable;
import java.util.concurrent.Callable;

import org.openengsb.labs.delegation.itests.bundles.provider.TestBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestProvider implements BundleActivator {

    public static class PageProvider implements Callable<Object> {
        private Object content;

        public PageProvider(Object content) {
            this.content = content;
        }

        @Override
        public Object call() throws Exception {
            return content;
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("type", "pageProvider");
        context.registerService(Callable.class.getName(), new PageProvider(new TestBean("foo")),
            properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
