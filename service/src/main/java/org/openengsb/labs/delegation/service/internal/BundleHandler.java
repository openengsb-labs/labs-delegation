package org.openengsb.labs.delegation.service.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.Provide;
import org.openengsb.labs.delegation.service.ResourceProvider;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleHandler.class);

    private static Map<Bundle, BundleHandler> bundleHandlers = new HashMap<Bundle, BundleHandler>();

    private Bundle bundle;
    private Map<String, Set<String>> providedClassesMap = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> providedResourcesMap = new HashMap<String, Set<String>>();

    private Set<String> bundleClasses;

    public static BundleHandler getInstance(Bundle bundle) {
        if (!bundleHandlers.containsKey(bundle)) {
            bundleHandlers.put(bundle, new BundleHandler(bundle));
        }
        return bundleHandlers.get(bundle);
    }

    public static void processBundle(Bundle bundle) {
        BundleHandler bundleHandler = getInstance(bundle);
        bundleHandler.scanBundle();
        bundleHandler.handle();
    }

    public static void injectIntoBundle(Bundle bundle, String context) {
        BundleHandler bundleHandler = getInstance(bundle);
        doRegisterClassProviderForBundle(bundle, bundleHandler.getAllClassesInBundle(), context);
    }

    public static void injectIntoBundle(Bundle bundle) {
        BundleHandler bundleHandler = getInstance(bundle);
        doRegisterClassProviderForBundle(bundle, bundleHandler.getAllClassesInBundle());
    }

    public static void injectIntoBundle(Bundle bundle, Collection<String> classFilters, String context) {
        BundleHandler bundleHandler = getInstance(bundle);
        Set<String> matchingClasses = bundleHandler.getMatchingClasses(classFilters);
        doRegisterClassProviderForBundle(bundle, matchingClasses, context);
    }

    public static void injectIntoBundle(Bundle bundle, Collection<String> classFilters) {
        BundleHandler bundleHandler = getInstance(bundle);
        Set<String> matchingClasses = bundleHandler.getMatchingClasses(classFilters);
        doRegisterClassProviderForBundle(bundle, matchingClasses);
    }

    public static void injectResourceProviderIntoBundle(Bundle bundle, Collection<String> fileFilters) {
        BundleHandler bundleHandler = getInstance(bundle);
        Set<String> matchingResources = bundleHandler.getMatchingResources(fileFilters);
        doRegisterResourceProvider(bundle, matchingResources);
    }

    public static void injectResourceProviderIntoBundle(Bundle bundle, Collection<String> fileFilters,
            String delegationContext) {
        BundleHandler bundleHandler = getInstance(bundle);
        Set<String> matchingResources = bundleHandler.getMatchingResources(fileFilters);
        doRegisterResourceProvider(bundle, matchingResources, delegationContext);
    }

    private BundleHandler(Bundle bundle) {
        this.bundle = bundle;
    }

    public void scanBundle() {
        providedResourcesMap.clear();
        providedClassesMap.clear();
        analyzePlainProvidesHeader();
        analyzeProvidesHeadersWithContext();
        analyzeAnnotations();
        checkResourcesHeader();
        checkResourcesHeaderWithContext();
    }

    public void handle() {
        LOGGER.info("injecting ClassProvider-Service into bundle {}.", bundle.getSymbolicName());
        if (providedClassesMap.containsKey("")) {
            Set<String> allClasses = providedClassesMap.remove("");
            doRegisterClassProviderForBundle(bundle, allClasses);
        }
        for (Map.Entry<String, Set<String>> entry : providedClassesMap.entrySet()) {
            doRegisterClassProviderForBundle(bundle, entry.getValue(), entry.getKey());
        }
        if (providedResourcesMap.containsKey("")) {
            Set<String> allResources = providedResourcesMap.remove("");
            doRegisterResourceProvider(bundle, allResources);
        }
        for (Map.Entry<String, Set<String>> entry : providedResourcesMap.entrySet()) {
            doRegisterResourceProvider(bundle, entry.getValue(), entry.getKey());
        }
    }

    private void analyzeAnnotations() {
        if (bundle.getHeaders().get(Constants.DELEGATION_ANNOTATIONS) == null) {
            return;
        }
        Set<String> discoverClasses = getAllClassesInBundle();
        for (String classname : discoverClasses) {
            Class<?> clazz;
            try {
                clazz = bundle.loadClass(classname);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("bundle could not find own class: " + classname, e);
                continue;
            }
            Provide provide = clazz.getAnnotation(Provide.class);
            if (provide == null) {
                continue;
            }
            for (String context : provide.value()) {
                addClassToContext(context, classname);
            }
        }
    }

    private void analyzeProvidesHeadersWithContext() {
        @SuppressWarnings("unchecked")
        Enumeration<String> keys = bundle.getHeaders().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (!key.startsWith(Constants.PROVIDED_CLASSES + "-")) {
                continue;
            }
            String context = key.replaceFirst(Constants.PROVIDED_CLASSES + "\\-", "");
            String providedClassesString = (String) bundle.getHeaders().get(key);
            Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
            Set<String> matchingClasses = getMatchingClasses(providedClasses);
            addClassesToContext(context, matchingClasses);
        }
    }

    private void analyzePlainProvidesHeader() {
        String providedClassesString = (String) bundle.getHeaders().get(Constants.PROVIDED_CLASSES);
        if (providedClassesString == null || providedClassesString.isEmpty()) {
            return;
        }
        Collection<String> providedClasses = parseProvidedClasses(providedClassesString);
        Set<String> matchingClasses = getMatchingClasses(providedClasses);
        addClassesToContext("", matchingClasses);
    }

    private void checkResourcesHeaderWithContext() {
        @SuppressWarnings("unchecked")
        Enumeration<String> keys = bundle.getHeaders().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (!key.startsWith(Constants.PROVIDED_RESOURCES + "-")) {
                continue;
            }
            String context = key.replaceFirst(Constants.PROVIDED_RESOURCES + "\\-", "");
            String providedResourcesString = (String) bundle.getHeaders().get(key);
            addAllResourcesToContext(providedResourcesString, context);
        }
    }

    private void addAllResourcesToContext(String providedResourcesString, String context) {
        Collection<String> resourceFilter = parseProvidedClasses(providedResourcesString);
        Set<String> matchingResources = getMatchingResources(resourceFilter);
        addResourcesToContext(context, matchingResources);
    }

    private void checkResourcesHeader() {
        String providedResourcesString = (String) bundle.getHeaders().get(Constants.PROVIDED_RESOURCES);
        if (providedResourcesString == null || providedResourcesString.isEmpty()) {
            return;
        }
        addAllResourcesToContext(providedResourcesString, "");
    }

    private Collection<String> parseProvidedClasses(String providedClassesString) {
        String[] providedClassesArray = providedClassesString.split(",");
        Collection<String> providedClassesList = new ArrayList<String>();
        for (String p : providedClassesArray) {
            providedClassesList.add(p.trim());
        }
        return providedClassesList;
    }

    private void addClassToContext(String context, String clazz) {
        if (!providedClassesMap.containsKey(context)) {
            providedClassesMap.put(context, new HashSet<String>());
        }
        providedClassesMap.get(context).add(clazz);
    }

    private void addResourcesToContext(String context, Set<String> resources) {
        if (!providedResourcesMap.containsKey(context)) {
            providedResourcesMap.put(context, resources);
        } else {
            providedResourcesMap.get(context).addAll(resources);
        }
    }

    private void addClassesToContext(String context, Set<String> classes) {
        if (!providedClassesMap.containsKey(context)) {
            providedClassesMap.put(context, classes);
        } else {
            providedClassesMap.get(context).addAll(classes);
        }
    }

    private static ClassProvider doRegisterClassProviderForBundle(Bundle b, Set<String> classes) {
        ClassProvider service = new ClassProviderImpl(b, classes);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.PROVIDED_CLASSES_KEY, classes);
        properties.put(Constants.CLASS_VERSION, b.getVersion().toString());
        b.getBundleContext().registerService(ClassProvider.class.getName(), service, properties);
        return service;
    }

    private static ClassProvider doRegisterClassProviderForBundle(Bundle b, Set<String> classes,
            String delegationContext) {
        ClassProvider service = new ClassProviderImpl(b, classes);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.PROVIDED_CLASSES_KEY, classes);
        properties.put(Constants.CLASS_VERSION, b.getVersion().toString());
        properties.put(Constants.DELEGATION_CONTEXT, delegationContext);
        b.getBundleContext().registerService(ClassProvider.class.getName(), service, properties);
        return service;
    }

    private static void doRegisterResourceProvider(Bundle bundle, Set<String> matchingResources) {
        ResourceProvider service = new ResourceProviderImpl(bundle, matchingResources);
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.PROVIDED_RESOURCES_KEY, matchingResources);
        properties.put(Constants.CLASS_VERSION, bundle.getVersion().toString());
        bundle.getBundleContext().registerService(ResourceProvider.class.getName(), service, properties);
    }

    private static void doRegisterResourceProvider(Bundle bundle, Set<String> matchingResources,
            String delegationContext) {
        ResourceProvider service = new ResourceProviderImpl(bundle, matchingResources);
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.PROVIDED_RESOURCES_KEY, matchingResources);
        properties.put(Constants.CLASS_VERSION, bundle.getVersion().toString());
        properties.put(Constants.DELEGATION_CONTEXT, delegationContext);
        bundle.getBundleContext().registerService(ResourceProvider.class.getName(), service, properties);
    }

    private Set<String> getMatchingClasses(Collection<String> classFilters) {
        Set<String> matchingClasses = new HashSet<String>();
        Collection<String> expressions = prepareFilterExpressions(classFilters);
        for (String classname : getAllClassesInBundle()) {
            for (String e : expressions) {
                if (Pattern.matches(e, classname)) {
                    matchingClasses.add(classname);
                    break;
                }
            }
        }
        return matchingClasses;
    }

    private Set<String> getMatchingResources(Collection<String> fileFilters) {
        Set<String> matchingFiles = new HashSet<String>();
        for (String p : fileFilters) {

            int lastIndexOf = p.lastIndexOf("/");
            String path = "/" + p.substring(0, lastIndexOf);
            String pattern = p.substring(lastIndexOf + 1);
            @SuppressWarnings("unchecked")
            Enumeration<URL> resources = bundle.findEntries(path, pattern, true);
            if (resources == null) {
                LOGGER.warn("no resources found for pattern", p);
                continue;
            }
            while (resources.hasMoreElements()) {
                matchingFiles.add(resources.nextElement().getPath().replaceFirst("^/", ""));
            }
        }
        return matchingFiles;
    }

    private Set<String> getAllClassesInBundle() {
        if (bundleClasses == null) {
            bundleClasses = discoverClasses(bundle);
        }
        return bundleClasses;
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

    private static String extractClassName(URL classURL) {
        String path = classURL.getPath();
        return path
            .replaceAll("^/", "")
            .replaceAll(".class$", "")
            .replaceAll("\\/", ".");
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

}
