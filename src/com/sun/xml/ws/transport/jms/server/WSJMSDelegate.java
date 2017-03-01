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

import com.sun.xml.ws.transport.jms.JMSConstants;
import com.sun.xml.ws.transport.jms.JMSURI;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.JMSException;

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
    private JMSAdapter getTarget(JMSURI jmsURI) throws JMSException {
        JMSAdapter result = null;
        String path = jmsURI.getParameter(JMSConstants.TARGET_SERVICE_URI);
        if (path != null) {
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
        JMSURI jmsURI = JMSURI.parse(targetURI);
        try {
            JMSAdapter target = getTarget(jmsURI);
            target.handle(context, message, jmsURI);
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
