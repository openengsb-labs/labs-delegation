package org.openengsb.labs.delegation.service.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.labs.delegation.api.ClassProvider;
import org.openengsb.labs.delegation.api.Constants;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.cm.Configuration;

public class FilterTest {

    @Mock
    private Bundle mainBundle;

    @Mock
    private BundleContext mainBundleContext;

    private List<BundleListener> listeners = new ArrayList<BundleListener>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mainBundle.getSymbolicName()).thenReturn("test.bundle");
        when(mainBundle.getBundleContext()).thenReturn(mainBundleContext);
        when(mainBundleContext.getBundle()).thenReturn(mainBundle);
        when(mainBundleContext.getBundles()).thenReturn(new Bundle[]{ mainBundle });

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                listeners.add((BundleListener) invocation.getArguments()[0]);
                return null;
            }
        }).when(mainBundleContext).addBundleListener(any(BundleListener.class));
        Activator activator = new Activator();
        activator.start(mainBundleContext);
    }

    @Test
    public void runActivator_shouldAddBundleListener() throws Exception {
        assertThat("No BundleListener was registered", listeners.isEmpty(), is(false));
    }

    @Test
    public void installBundle_shouldRegisterService() throws Exception {
        Bundle bundle2 = mock(Bundle.class);
        BundleContext bundle2Context = mock(BundleContext.class);
        when(bundle2.getBundleContext()).thenReturn(bundle2Context);
        when(bundle2.getHeaders()).thenReturn(new Hashtable<String, Object>());
        Dictionary<String, Object> headers = new Hashtable<String, Object>();
        headers.put(Constants.PROVIDED_CLASSES, "*");
        when(bundle2.getHeaders()).thenReturn(headers);
        installBundle(bundle2);
        verify(bundle2Context).registerService(eq(ClassProvider.class.getName()), any(ClassProvider.class),
            any(Dictionary.class));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void classProvider_shouldProvideClass() throws Exception {
        Bundle bundle2 = mock(Bundle.class);
        BundleContext bundle2Context = mock(BundleContext.class);
        when(bundle2.getBundleContext()).thenReturn(bundle2Context);
        Dictionary<String, Object> headers = new Hashtable<String, Object>();
        headers.put(Constants.PROVIDED_CLASSES, "*");
        when(bundle2.getHeaders()).thenReturn(headers);
        when(bundle2.loadClass(Bundle.class.getName())).thenReturn(Bundle.class);
        ArgumentCaptor<ClassProvider> captor = ArgumentCaptor.forClass(ClassProvider.class);
        installBundle(bundle2);
        verify(bundle2Context).registerService(eq(ClassProvider.class.getName()), captor.capture(),
            any(Dictionary.class));
        ClassProvider provider = captor.getValue();
        assertThat(provider.loadClass(Bundle.class.getName()), equalTo((Class) Bundle.class));
    }

    @Test
    public void installBundleWithHeaders_shouldProvideOnlySpecificClasses() throws Exception {
        Bundle bundle2 = mock(Bundle.class);
        BundleContext bundle2Context = mock(BundleContext.class);
        when(bundle2.getBundleContext()).thenReturn(bundle2Context);
        Dictionary<String, Object> headers = new Hashtable<String, Object>();
        headers.put(Constants.PROVIDED_CLASSES, "org.osgi.framework.*");
        when(bundle2.getHeaders()).thenReturn(headers);
        when(bundle2.loadClass(Configuration.class.getName())).thenReturn(Configuration.class);
        ArgumentCaptor<ClassProvider> captor = ArgumentCaptor.forClass(ClassProvider.class);
        installBundle(bundle2);
        verify(bundle2Context).registerService(eq(ClassProvider.class.getName()), captor.capture(),
            any(Dictionary.class));
        ClassProvider provider = captor.getValue();
        try {
            provider.loadClass(Configuration.class.getName());
            fail("Expected class not to be found");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void injectServiceToForeignBundle_shouldProvideClasses() throws Exception {
        Bundle bundle2 = mock(Bundle.class);
        BundleContext bundle2Context = mock(BundleContext.class);
        when(bundle2.getBundleContext()).thenReturn(bundle2Context);
        when(bundle2.loadClass(Configuration.class.getName())).thenReturn(Configuration.class);
        when(bundle2.getHeaders()).thenReturn(new Hashtable<String, Object>());
        installBundle(bundle2);
        ClassProvider provider = DelegationUtil.registerClassProviderForBundle(bundle2);
        assertThat(provider.loadClass(Configuration.class.getName()), equalTo((Class) Configuration.class)); 
    }

    private void installBundle(Bundle bundle2) {
        BundleEvent installedEvent = new BundleEvent(BundleEvent.STARTED, bundle2);
        for (BundleListener bl : listeners) {
            bl.bundleChanged(installedEvent);
        }
    }
}
