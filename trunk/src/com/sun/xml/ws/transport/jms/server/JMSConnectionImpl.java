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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.jms.JMSConstants;
import com.sun.xml.ws.transport.jms.JMSURI;
import com.sun.xml.ws.transport.jms.JMSUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * @author Alexey Stashok
 */
public class JMSConnectionImpl implements WebServiceContextDelegate {
    
    private JMSContext context;
    private Session jmsSession;
    private BytesMessage requestMessage;
    private JMSURI uri;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    private int status;
    
    private boolean isClosed;
    
    private Map<String, String> requestHeaders;
    private Map<String, String> responseHeaders;
    
    public JMSConnectionImpl(JMSContext context, BytesMessage requestMessage, JMSURI uri) throws JMSException {
        this.context = context;
        this.requestMessage = requestMessage;
        this.uri = uri;
        jmsSession = (Session) context.getAttribute(JMSContext.SESSION_ATTR);
        
        BytesMessage rqstMessage = (BytesMessage) requestMessage;
        byte[] rqstBuf = new byte[(int) rqstMessage.getBodyLength()];
        rqstMessage.readBytes(rqstBuf);
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream(rqstBuf);
        
        populateRequestHeaders();
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int statusCode) {
        this.status = statusCode;
    }
    
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }
    
    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
    
    public String getRequestHeader(String name) {
        return requestHeaders.get(name);
    }
    
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
    
    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public OutputStream getOutputStream() {
        return outputStream;
    }
    
    public void closeInput() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ex) {
            }
            
            inputStream = null;
        }
    }
    
    public void closeOutput() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException ex) {
            }
            
            outputStream = null;
        }
    }
    
    public void close() {
        if (!isClosed) {
            synchronized (this) {
                if (!isClosed) {
                    isClosed = true;
                    
                    closeInput();
                    closeOutput();
                }
            }
        }
    }
    
    // Not supported
    public Principal getUserPrincipal(Packet request) {
        return null;
    }
    
    // Not supported
    public boolean isUserInRole(Packet request, String role) {
        return false;
    }
    
    public @NotNull String getEPRAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
        return uri.toString();
    }

    public void flush() throws IOException, JMSException {
        if (outputStream != null) {
            outputStream.flush();
        }
        
        BytesMessage replyMessage = jmsSession.createBytesMessage();
        replyMessage.setIntProperty(JMSConstants.REPLY_STATUS_PROPERTY, status);
        if (!JMSUtils.isStatusError(status) && status != JMSConstants.ONEWAY) {
            byte[] content = ((ByteArrayOutputStream) getOutputStream()).toByteArray();
            if (content.length > 0) {
                replyMessage.writeBytes(content);
            }
            
            for(Map.Entry<String, String> entry : responseHeaders.entrySet()) {
                if (entry.getValue() != null) {
                    replyMessage.setStringProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        
        Session jmsSession = (Session) context.getAttribute(JMSContext.SESSION_ATTR);
        Queue replyQueue = (Queue) requestMessage.getJMSReplyTo();
        jmsSession.createProducer(replyQueue).send(replyMessage);
    }
    
    private void populateRequestHeaders() throws JMSException {
        requestHeaders = new HashMap();
        
        for(Enumeration e = requestMessage.getPropertyNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            String value = requestMessage.getStringProperty(name);
            requestHeaders.put(name, value);
        }
    }
}
