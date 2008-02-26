/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.jms.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
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
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Alexey Stashok
 */
public class JMSStandaloneContext implements JMSContext {
    private ClassLoader classloader;
    private Map<String, Object> attributes = new HashMap();
    private transient Set<String> resourcePaths;
    
    private InitialContext jndiContext;
    public JMSStandaloneContext(InitialContext jndiContext,
            ClassLoader classloader) {
        this.classloader = classloader;
        this.jndiContext = jndiContext;
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
        return classloader.getResource(trimRecourcePath(resource));
    }
    
    private Set<String> populateResourcePaths(String path) throws Exception {
        Set<String> resourcePathes = new HashSet<String>();
        
        Enumeration<URL> urlEnum = classloader.getResources(trimRecourcePath(path));
        while(urlEnum.hasMoreElements()) {
            URI resourceURI = urlEnum.nextElement().toURI();
            
            if (resourceURI.getScheme().equals("file")) {
                resourcePathes.addAll(gatherResourcesWithFileMode(path, resourceURI));
            } else if (resourceURI.getScheme().equals("jar")) {
                resourcePathes.addAll(gatherResourcesWithJarMode(path, resourceURI));
            }
        }
        
        return resourcePathes;
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
        
        String zipFile;

        if (File.separatorChar == '/') {
            zipFile = resourceURIAsString.substring("jar:file:".length(), (pathDelim != -1) ? pathDelim : resourceURIAsString.length());
        } else {
            zipFile = resourceURIAsString.substring("jar:file:/".length(), (pathDelim != -1) ? pathDelim : resourceURIAsString.length());
        }
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

    public Object lookup(String name) throws NamingException {
        return jndiContext.lookup(name);
    }
    
    private String trimRecourcePath(String resource) {
        if (resource.startsWith("/")) {
            resource = resource.substring(1, resource.length());
        }
        
        return resource;
    }
}
