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

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import org.openengsb.labs.delegation.service.internal.ClassloadingDelegateImpl;
import org.osgi.framework.Bundle;

public final class DelegationUtil {

    public static ClassProvider registerClassProviderForBundle(Bundle b) {
        return registerClassProviderForBundle(b, Arrays.asList("*"));
    }

    public static ClassProvider registerClassProviderForBundle(Bundle b, Collection<String> classFilter) {
        ClassProvider service = new ClassloadingDelegateImpl(b, classFilter);
        b.getBundleContext().registerService(ClassProvider.class.getName(), service, new Hashtable<String, Object>());
        return service;
    }

    private DelegationUtil() {
    }

}
