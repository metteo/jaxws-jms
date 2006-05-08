/*
 * WSJMSDelegate.java
 */

package com.sun.xml.ws.transport.jms.server;

import com.sun.xml.ws.transport.jms.JMSConstants;
import com.sun.xml.ws.transport.jms.JMSURI;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Alexey Stashok
 */

public class WSJMSDelegate {
    private List<JMSAdapter> adapters;
    private JMSContext context;
    
    private final Map<String, JMSAdapter> fixedUrlPatternEndpoints = new HashMap<String, JMSAdapter>();
    private final List<JMSAdapter> pathUrlPatternEndpoints = new ArrayList<JMSAdapter>();
    
    
    public WSJMSDelegate(List<JMSAdapter> adapters, JMSContext context) {
        this.adapters = adapters;
        this.context = context;
        
        for(JMSAdapter info : adapters)
            registerEndpointUrlPattern(info);
    }
    
    private void registerEndpointUrlPattern(JMSAdapter adapter) {
        String urlPattern = adapter.urlPattern;
        
        if (urlPattern.endsWith("/*")) {
            pathUrlPatternEndpoints.add(adapter);
        } else {
            if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                // Warning because of duplication
            } else {
                fixedUrlPatternEndpoints.put(urlPattern, adapter);
            }
        }
    }
    
    /**
     * Determines which {@link ServletAdapter} serves the given request.
     */
    private JMSAdapter getTarget(String requestURI) throws JMSException {
        JMSAdapter result = null;
        JMSURI jmsURI = JMSURI.parse(requestURI);
        String path = jmsURI.getParameter(JMSConstants.TARGET_SERVICE_URI);
        if (path != null) {
            path = "/" + path.split("/")[1];
            
            result = fixedUrlPatternEndpoints.get(path);
            if (result == null) {
                for (JMSAdapter candidate : pathUrlPatternEndpoints) {
                    if (path.startsWith(candidate.getValidPath())) {
                        result = candidate;
                        break;
                    }
                }
            }
        }
        
        return result;
    }
    
    public void process(BytesMessage message) throws JMSException {
        String targetURI = message.getStringProperty(JMSConstants.TARGET_URI_PROPERTY);
        
        try {
            JMSAdapter target = getTarget(targetURI);
            target.handle(context, message);
        } catch (JAXWSExceptionBase e) {
//            try {
//                replyMessage.setIntProperty(REPLY_STATUS_PROPERTY, RS_INTERNAL_SERVER_ERROR);
//            } catch (JMSException ex) {
//            }
        } catch (Throwable e) {
//            try {
//                replyMessage.setIntProperty(REPLY_STATUS_PROPERTY, RS_INTERNAL_SERVER_ERROR);
//            } catch (JMSException ex) {
//            }
        }
    }
}
