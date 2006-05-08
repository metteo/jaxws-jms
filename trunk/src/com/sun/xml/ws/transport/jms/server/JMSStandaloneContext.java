/*
 * JMSStandaloneContext.java
 */

package com.sun.xml.ws.transport.jms.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Alexey Stashok
 */
public class JMSStandaloneContext implements JMSContext {
    private ClassLoader classloader;
    private Map<String, Object> attributes = new HashMap();
    private Set<String> resourcePaths;
    
    public JMSStandaloneContext(ClassLoader classloader) {
        this.classloader = classloader;
    }
    
    public InputStream getResourceAsStream(String resource) throws IOException {
        return classloader.getResourceAsStream(resource);
    }
    
    public Set<String> getResourcePaths(String path) {
        if (resourcePaths == null) {
            synchronized (this) {
                if (resourcePaths == null) {
                    try {
                        resourcePaths = populateResourcePaths(path);
                    } catch (Exception ex) {
                        resourcePaths = Collections.emptySet();
                    }
                }
            }
        }
        
        return resourcePaths;
    }
    
    public URL getResource(String resource) {
        if (resource.startsWith("/")) {
            resource = resource.substring(1, resource.length());
        }
        
        return classloader.getResource(resource);
    }
    
    private Set<String> populateResourcePaths(String path) throws Exception {
        URL initResource = getResource(path);
        URI resourceURI = initResource.toURI();
        if (resourceURI.getScheme().equals("file")) {
            return gatherResourcesWithFileMode(path, resourceURI);
        } else if (resourceURI.getScheme().equals("jar")) {
            return gatherResourcesWithJarMode(path, resourceURI);
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<String> gatherResourcesWithFileMode(String path, URI resourceURI) {
        File file = new File(resourceURI);
        String[] list = file.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return !name.startsWith(".");
            }
        });
        
        Set<String> resources = new HashSet<String>(list.length);
        for(String filename : list) {
            resources.add(path + filename);
        }
        
        return resources;
    }
    
    private Set<String> gatherResourcesWithJarMode(String path, URI resourceURI) {
        String resourceURIAsString = resourceURI.toASCIIString();
        int pathDelim = resourceURIAsString.indexOf("!");
        String zipFile = resourceURIAsString.substring("jar:file:/".length(), (pathDelim != -1) ? pathDelim : resourceURIAsString.length());
        ZipFile file = null;
        
        Set<String> resources = new HashSet();
        try {
            file = new ZipFile(zipFile);
            
            String pathToCompare = path;
            if (pathToCompare.startsWith("/")) {
                pathToCompare = pathToCompare.substring(1, pathToCompare.length());
            }
            if (!pathToCompare.endsWith("/")) {
                pathToCompare = pathToCompare + "/";
            }
            
            for(Enumeration<? extends ZipEntry> e = file.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                if (entry.getName().startsWith(pathToCompare) && !entry.getName().equals(pathToCompare)) {
                    resources.add("/" + entry.getName());
                }
            }
        } catch(IOException e) {
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ex) {
                }
            }
        }
        
        return resources;
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
}
