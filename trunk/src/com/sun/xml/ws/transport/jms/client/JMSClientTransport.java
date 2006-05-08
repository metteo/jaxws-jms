/*
 * JMSClientTransport.java
 */

package com.sun.xml.ws.transport.jms.client;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.client.ClientTransportException;
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
        ConnectionFactory connectionFactory = null;
        Queue dest = null;
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        BytesMessage message = null;
        
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
                throw new ClientTransportException("jms.client.failed", e);
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
            throw new ClientTransportException("jms.client.failed", e);
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
                    throw new ClientTransportException("jms.status.code", status);
                }
                
                if (isPayloadExist()) {
                    byte[] buffer = new byte[(int) bmReplyMessage.getBodyLength()];
                    ((BytesMessage) bmReplyMessage).readBytes(buffer);
                    return buffer;
                }
            }
        } catch (JMSException e) {
            throw new ClientTransportException("jms.client.failed", e);
        } finally {
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
        responseHeaders = new HashMap();
        
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
