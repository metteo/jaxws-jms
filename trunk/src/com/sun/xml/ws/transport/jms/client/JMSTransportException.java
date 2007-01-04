package com.sun.xml.ws.transport.jms.client;

import javax.xml.ws.WebServiceException;

/**
 * Signals an error in JMS transport.
 *
 * @author Kohsuke Kawaguchi
 */
public class JMSTransportException extends WebServiceException {

    public JMSTransportException(String message) {
        super(message);
    }

    public JMSTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public JMSTransportException(Throwable cause) {
        super(cause);
    }
}
