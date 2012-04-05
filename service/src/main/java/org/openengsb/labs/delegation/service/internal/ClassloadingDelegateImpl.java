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
import java.util.HashSet;
import java.util.Set;

import org.openengsb.labs.delegation.service.ClassProvider;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to provide a Simple Delegated class-loading provider, configurable as a bean.
 */
public class ClassloadingDelegateImpl implements ClassProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassloadingDelegateImpl.class);
    private Set<String> classes = new HashSet<String>();

    protected Bundle bundle;

    public ClassloadingDelegateImpl(Bundle bundle, Set<String> classes) {
        this.bundle = bundle;
        this.classes = classes;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        LOGGER.debug("loading class {} by delegation", name);
        if(classes.contains(name)){
            return bundle.loadClass(name);
        }
        throw new ClassNotFoundException("Could not find class " + name + " using service " + this);
    }

    @Override
    public URL loadResource(String name) {
        LOGGER.debug("loading ressource {} by delegation", name);
        return bundle.getResource(name);
    }

}
