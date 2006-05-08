/*
 * JMSContext.java
 */

package com.sun.xml.ws.transport.jms.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

/**
 * @author Alexey Stashok
 */

public interface JMSContext {
    public static final String SESSION_ATTR = "JMS_SESSION";

    public URL getResource(String resource);
    public InputStream getResourceAsStream(String resource) throws IOException;
    public Set<String> getResourcePaths(String path);
    public Object getAttribute(String name);
    public void setAttribute(String name, Object value);
}
