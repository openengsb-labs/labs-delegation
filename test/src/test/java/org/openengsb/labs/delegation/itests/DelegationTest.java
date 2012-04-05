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

package org.openengsb.labs.delegation.itests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.allFrameworks;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.delegation.itests.bundles.consumer.internal.TestConsumer;
import org.openengsb.labs.delegation.itests.bundles.provider.ChildBean;
import org.openengsb.labs.delegation.itests.bundles.provider.TestBean;
import org.openengsb.labs.delegation.itests.bundles.provider.TestService;
import org.openengsb.labs.delegation.itests.bundles.provider.internal.TestProvider;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class DelegationTest {

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return options(
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject(),
            mavenBundle().groupId("org.openengsb.labs.delegation").artifactId("org.openengsb.labs.delegation.service")
                .versionAsInProject(),
            junitBundles(),
            allFrameworks());
    }

    @Test
    public void loadPrivateClassFromProvider_consumerShouldLoadClass() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();

        TinyBundle tinyBundle = createConsumerBundle();
        Bundle consumerBundle =
            bundleContext.installBundle("test://testlocation/test.consumer.jar", tinyBundle.build());
        consumerBundle.start();

        @SuppressWarnings("unchecked")
        Callable<Method> resultTask = getOsgiService(Callable.class, "(resultProvider=test)");
        Method method = resultTask.call();

        assertThat(method.getName(), is("doSomething"));
        assertThat(method.getParameterTypes(), equalTo(new Class<?>[0]));
    }

    @Test
    public void stopProviderBundle_shouldUnregisterServices() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();

        TinyBundle tinyBundle = createConsumerBundle();
        Bundle consumerBundle =
            bundleContext.installBundle("test://testlocation/test.consumer.jar", tinyBundle.build());
        consumerBundle.start();

        ServiceReference serviceReference = bundleContext.getServiceReference(ClassProvider.class.getName());
        assertThat(serviceReference, not(nullValue()));

        providerBundle.stop();
        serviceReference = bundleContext.getServiceReference(ClassProvider.class.getName());
        assertThat(serviceReference, nullValue());
    }

    @Test
    public void provideBundleHeader_shouldOnlyProvideSpecifiedClasses() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES,
            TestBean.class.getName());
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider provider = getOsgiService(ClassProvider.class);
        provider.loadClass(TestBean.class.getName());
        try {
            provider.loadClass(ChildBean.class.getName());
            fail("expected class not to be found");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test
    public void provideBundleHeaderAndListClasses_shouldProvideSpecifiedClassesInList() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES,
            TestBean.class.getName());
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider provider = getOsgiService(ClassProvider.class);
        Collection<Class<?>> listClasses = provider.listClasses();
        for (Class<?> cls : listClasses) {
            if (cls.getName().equals(TestBean.class.getName())) {
                return;
            }
        }
        fail("class-list did not contain required item");
    }

    @Test
    public void provideBundleHeader_shouldProvideServiceWithProvidedClassesInProperties() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES,
            TestBean.class.getName());
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider provider =
            getOsgiService(ClassProvider.class, String.format("(%s=%s)",
                org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_KEY, TestBean.class.getName()));
        provider.loadClass(TestBean.class.getName());
        try {
            provider.loadClass(ChildBean.class.getName());
            fail("expected class not to be found");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test
    public void provideBundleHeaderWithContext_shouldOnlyProvideClassInContext() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES + "-foo",
            TestBean.class.getName());
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider provider =
            getOsgiService(ClassProvider.class, String.format("(&(%s=%s)(%s=%s))",
                org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_KEY, TestBean.class.getName(),
                org.openengsb.labs.delegation.service.Constants.DELEGATION_CONTEXT, "foo"));
        provider.loadClass(TestBean.class.getName());
        try {
            provider.loadClass(ChildBean.class.getName());
            fail("expected class not to be found");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test
    public void provideBundleWithAnnotations_shouldProvideClasses() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.DELEGATION_ANNOTATIONS, "true");
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider provider =
            getOsgiService(ClassProvider.class, String.format("(%s=%s)",
                org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_KEY, TestBean.class.getName()));
        provider.loadClass(TestBean.class.getName());
    }

    @Test
    public void provideBundleWithAnnotationsInContext_shouldProvideClassesInContext() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.DELEGATION_ANNOTATIONS, "true");
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider provider =
            getOsgiService(ClassProvider.class, String.format("(&(%s=%s)(%s=%s))",
                org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_KEY, TestBean.class.getName(),
                org.openengsb.labs.delegation.service.Constants.DELEGATION_CONTEXT, "foo"));
        provider.loadClass(TestBean.class.getName());
        try {
            provider.loadClass(ChildBean.class.getName());
            fail("expected class not to be found");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test
    public void reinstallProviderBundle_shouldReregisterServices() throws Exception {
        TinyBundle providerTinyBundle = createProviderBundle();
        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();

        TinyBundle tinyBundle = createConsumerBundle();
        Bundle consumerBundle =
            bundleContext.installBundle("test://testlocation/test.consumer.jar", tinyBundle.build());
        consumerBundle.start();

        ServiceReference serviceReference = bundleContext.getServiceReference(ClassProvider.class.getName());
        assertThat(serviceReference, not(nullValue()));

        providerTinyBundle.set(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES,
            TestService.class.getName());
        providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        ClassProvider service = getOsgiService(ClassProvider.class, String.format("(%s=%s)",
            org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_KEY, TestService.class.getName()));
        assertThat(service, not(nullValue()));
    }

    private TinyBundle createProviderBundle() {
        TinyBundle providerTinyBundle =
            bundle()
                .add(TestProvider.class)
                .add(TestProvider.PageProvider.class)
                .add(TestService.class)
                .add(TestBean.class)
                .add(ChildBean.class)
                .set(Constants.BUNDLE_ACTIVATOR, TestProvider.class.getName())
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.provider")
                .set(Constants.BUNDLE_VERSION, "1.0.0")
                .set(Constants.IMPORT_PACKAGE,
                    "org.osgi.framework, org.slf4j, org.openengsb.labs.delegation.service");
        return providerTinyBundle;
    }

    private TinyBundle createConsumerBundle() {
        TinyBundle tinyBundle =
            bundle()
                .add(TestConsumer.class)
                .add(TestConsumer.ResultTask.class)
                .set(Constants.BUNDLE_ACTIVATOR, TestConsumer.class.getName())
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.consumer")
                .set(Constants.BUNDLE_VERSION, "1.0.0")
                .set(Constants.IMPORT_PACKAGE,
                    "org.osgi.framework, org.slf4j, org.openengsb.labs.delegation.service");
        return tinyBundle;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOsgiService(Class<T> serviceClass) throws InvalidSyntaxException, InterruptedException {
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, serviceClass.getName(), null);
        serviceTracker.open();
        return (T) serviceTracker.waitForService(5000);
    }

    @SuppressWarnings("unchecked")
    private <T> T getOsgiService(Class<T> serviceClass, String filter) throws InvalidSyntaxException,
        InterruptedException {
        String filterString = String.format("(&(%s=%s)%s)", Constants.OBJECTCLASS, serviceClass.getName(), filter);
        Filter filterObj = FrameworkUtil.createFilter(filterString);
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, filterObj, null);
        serviceTracker.open();
        return (T) serviceTracker.waitForService(5000);
    }
}
