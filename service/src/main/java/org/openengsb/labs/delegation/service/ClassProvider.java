package org.openengsb.labs.delegation.service;

import java.net.URL;

public interface ClassProvider {

    Class<?> loadClass(String name) throws ClassNotFoundException;

    URL loadRessource(String name);

}
