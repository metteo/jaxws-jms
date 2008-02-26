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

package com.sun.xml.ws.transport.jms.client;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.transport.jms.JMSConstants;
import com.sun.xml.ws.transport.jms.JMSURI;
import com.sun.xml.ws.transport.jms.JMSUtils;
import java.net.URI;
import java.util.*;
import javax.jms.*;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Alexey Stashok
 */
public class JMSClientTransport {
    private static final long TIMEOUT_INTERVAL = 60000;
    private static JMSConnectionCache connectionCache = new JMSConnectionCache();
    
    private Packet packet;
    
    private Map<String, String> requestHeaders;
    private Map<String, String> responseHeaders;
    private Message replyMessage;
    private int status;
    
    public JMSClientTransport(Packet packet, Map<String, String> requestHeaders) {
        this.packet = packet;
        this.requestHeaders = requestHeaders;
    }
    
    public int getStatus() {
        return status;
    }
    
    byte[] sendMessage(byte[] sndPacket) {
        InitialContext ic = null;
        ConnectionFactory connectionFactory;
        Queue dest;
        Connection connection = null;
        Session session = null;
        MessageProducer producer;
        MessageConsumer consumer;
        BytesMessage message;
        
        URI uri = packet.endpointAddress.getURI();
        JMSURI destinationAddress = JMSURI.parse(uri);
        
        String jmsConnectionAddress = destinationAddress.host + ":" + destinationAddress.port;
        JMSConnectionCache.JMSConnectionRecord cachedConnectionRecord = connectionCache.getJMSConnection(jmsConnectionAddress);
        
        if (cachedConnectionRecord == null) {
            try {
                Properties env = new Properties();
                env.put("org.omg.CORBA.ORBInitialHost", destinationAddress.host);
                env.put("org.omg.CORBA.ORBInitialPort", String.valueOf(destinationAddress.port));
                
                ic = new InitialContext(env);
                
                cachedConnectionRecord = new JMSConnectionCache.JMSConnectionRecord(ic);
                connectionCache.putJMSConnectionRecord(jmsConnectionAddress, cachedConnectionRecord);
            } catch (NamingException e) {
                throw new JMSTransportException(e);
            }
        }
        
        try {
            connectionFactory = cachedConnectionRecord.getConnectionFactory(destinationAddress.factory);
            if (connectionFactory == null) {
                connectionFactory =
                        (ConnectionFactory) ic.lookup(destinationAddress.factory);
                cachedConnectionRecord.putConnectionFactory(destinationAddress.factory, connectionFactory);
            }
            
            dest = cachedConnectionRecord.getQueue(destinationAddress.queue);
            if (dest == null) {
                dest = (Queue) ic.lookup(destinationAddress.queue);
                cachedConnectionRecord.putQueue(destinationAddress.queue, dest);
            }
        } catch (NamingException e) {
            throw new JMSTransportException("Failed to obtain queue "+destinationAddress, e);
        }
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(dest);
            
            Queue tempQueue = session.createTemporaryQueue();
            consumer = session.createConsumer(tempQueue);
            connection.start();
            
            message = session.createBytesMessage();
            populateRequestHeaders(message);
            
            message.setJMSReplyTo(tempQueue);
            message.writeBytes(sndPacket);
            producer.send(message);
            replyMessage = consumer.receive(TIMEOUT_INTERVAL);
            if (replyMessage != null && replyMessage instanceof BytesMessage) {
                BytesMessage bmReplyMessage = (BytesMessage) replyMessage;
                populateResponseHeaders(bmReplyMessage);
                if (JMSUtils.isStatusError(status)) {
                    throw new JMSTransportException("Error response received "+status);
                }
                
                if (isPayloadExist()) {
                    byte[] buffer = new byte[(int) bmReplyMessage.getBodyLength()];
                    bmReplyMessage.readBytes(buffer);
                    return buffer;
                }
            }
        } catch (JMSException e) {
            throw new JMSTransportException("Error getting a response", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException ex) {
                }
            }
            
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
        
        return null;
    }
    
    public boolean isPayloadExist() {
        return !JMSUtils.isStatusError(status) && status != JMSConstants.ONEWAY;
    }
    
    private void populateRequestHeaders(Message message) throws JMSException {
        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            message.setStringProperty(entry.getKey(), entry.getValue());
        }
    }
    
    private void populateResponseHeaders(Message message) throws JMSException {
        responseHeaders = new HashMap<String,String>();
        
        status = message.getIntProperty(JMSConstants.REPLY_STATUS_PROPERTY);
        
        if (!JMSUtils.isStatusError(status) && status != JMSConstants.ONEWAY) {
            for (Enumeration e = message.getPropertyNames(); e.hasMoreElements(); ) {
                String name = (String) e.nextElement();
                Object value = message.getObjectProperty(name);
                responseHeaders.put(name, value.toString());
            }
        }
    }
    
    Map<String, String> getHeaders() {
        if (responseHeaders == null) {
            try {
                populateResponseHeaders(replyMessage);
            } catch (JMSException ex) {
            }
        }
        
        return responseHeaders;
    }
    
    
}
