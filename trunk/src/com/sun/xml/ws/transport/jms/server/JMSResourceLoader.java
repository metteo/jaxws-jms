/*
 * JMSResourceLoader.java
 */
package com.sun.xml.ws.transport.jms.server;

import com.sun.xml.ws.transport.http.ResourceLoader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * @author Alexey Stashok
 */
public class JMSResourceLoader implements ResourceLoader {
    private JMSContext context;
    
    public JMSResourceLoader(JMSContext context) {
        this.context = context;
    }
    
    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }

    public URL getCatalogFile() throws MalformedURLException {
        return getResource("/WEB-INF/jax-ws-catalog.xml");
    }

    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }
}
