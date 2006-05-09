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
