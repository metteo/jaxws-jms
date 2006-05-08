/*
 * WSJMSException.java
 */

package com.sun.xml.ws.transport.jms.server;

/**
 * @author Alexey Stashok
 */

public class WSJMSException extends RuntimeException {
    
    public WSJMSException() {
    }
    
    public WSJMSException(String reason) {
        super(reason);
    }
    
    public WSJMSException(String reason, Throwable trace) {
        super(reason, trace);
    }
}
