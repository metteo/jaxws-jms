/*
 * JMSURI.java
 */

package com.sun.xml.ws.transport.jms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public class JMSURI {
    public String host;
    public int port;
    public String factory;
    public String queue;
    private Map<String, String> params;
    
    private JMSURI(String host, int port, String factory, String queue, Map<String, String> params) {
        this.host = host;
        this.port = port;
        this.factory = factory;
        this.queue = queue;
        this.params = params;
    }
    
    public String getParameter(String name) {
        if (params != null) {
            return params.get(name);
        }
        
        return null;
    }
    
    public static JMSURI parse(String uri) {
        try {
            return parse(new URI(uri));
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    public static JMSURI parse(URI uri) {
        String path = uri.getPath().substring(1, uri.getPath().length());
        String[] sprt = path.split("/");
        String query = uri.getQuery();
        Map<String, String> params = null;
        
        if (query != null && query.length() > 0) {
            params = new HashMap();
            String[] paramsStr = query.split(";");
            for(String paramStr : paramsStr) {
                if (paramStr.length() > 0) {
                    String[] paramAsgn = paramStr.split("=");
                    if (paramAsgn != null && paramAsgn.length == 2 && paramAsgn[0].length() > 0 && paramAsgn[1].length() > 0) {
                        params.put(paramAsgn[0], paramAsgn[1]);
                    }
                }
            }
        }
        
        return new JMSURI(uri.getHost(), uri.getPort(), sprt[0], sprt[1], params);
    }
    
}
