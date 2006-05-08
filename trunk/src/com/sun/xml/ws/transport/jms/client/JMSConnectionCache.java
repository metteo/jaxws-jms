/*
 * JMSConnectionCache.java
 */

package com.sun.xml.ws.transport.jms.client;

import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

/**
 * @author Alexey Stashok
 */

public class JMSConnectionCache {
    
    private Map<String, JMSConnectionRecord> cache = new HashMap();
    
    public JMSConnectionCache() {
    }
    
    public JMSConnectionRecord getJMSConnection(String address) {
        return cache.get(address);
    }
    
    public void putJMSConnectionRecord(String address, JMSConnectionRecord record) {
        cache.put(address, record);
    }
    
    public static class JMSConnectionRecord {
        InitialContext context;
        Map<String, ConnectionFactory> factoryCache = new HashMap();
        Map<String, Queue> queueCache = new HashMap();
        
        public JMSConnectionRecord(InitialContext context) {
            this.context = context;
        }
        
        public void putConnectionFactory(String factoryName, ConnectionFactory factory) {
            factoryCache.put(factoryName, factory);
        }
        
        public ConnectionFactory getConnectionFactory(String factoryName) {
            return factoryCache.get(factoryName);
        }
        
        public void putQueue(String queueName, Queue queue) {
            queueCache.put(queueName, queue);
        }
        
        public Queue getQueue(String queueName) {
            return queueCache.get(queueName);
        }
    }
}
