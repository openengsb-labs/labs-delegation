package org.openengsb.labs.delegation.itests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.util.Arrays;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.delegation.itests.bundles.consumer.internal.TestConsumer;
import org.openengsb.labs.delegation.itests.bundles.provider.TestService;
import org.openengsb.labs.delegation.itests.bundles.provider.internal.TestProvider;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.tinybundles.core.InnerClassStrategy;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

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
            felix());
    }

    @Test
    public void installBundle() throws Exception {
        TinyBundle tinyBundle =
            bundle()
                .add(TestConsumer.class, InnerClassStrategy.ALL)
                .add(TestConsumer.DummyInvocationHandler.class, InnerClassStrategy.ALL)
                .set(Constants.BUNDLE_ACTIVATOR, TestConsumer.class.getName())
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.consumer")
                .set(Constants.BUNDLE_VERSION, "1.0.0")
                .set(Constants.IMPORT_PACKAGE,
                    "org.osgi.framework, org.slf4j, org.openengsb.labs.delegation.service");

        Bundle consumerBundle =
            bundleContext.installBundle("test://testlocation/test.consumer.jar", tinyBundle.build());

        TinyBundle providerTinyBundle =
            bundle()
                .add(TestProvider.class)
                .add(TestService.class)
                .set(Constants.BUNDLE_ACTIVATOR, TestProvider.class.getName())
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.provider")
                .set(Constants.BUNDLE_VERSION, "1.0.0")
                .set(Constants.IMPORT_PACKAGE,
                    "org.osgi.framework, org.slf4j, org.openengsb.labs.delegation.service");

        Bundle providerBundle =
            bundleContext.installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());

        providerBundle.start();
        consumerBundle.start();

        ServiceReference[] registeredServices = consumerBundle.getRegisteredServices();
        assertNotNull("bundle had no services", registeredServices);
        for (ServiceReference ref : registeredServices) {
            String[] interfaces = (String[]) ref.getProperty(Constants.OBJECTCLASS);
            if (Arrays.asList(interfaces).contains(TestService.class.getName())) {
                return;
            }
        }
        fail("service not registerd");
    }
}
