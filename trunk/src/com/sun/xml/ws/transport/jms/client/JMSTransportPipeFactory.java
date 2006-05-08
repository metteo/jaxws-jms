/*
 * JMSTransportPipeFactory.java
 */

package com.sun.xml.ws.transport.jms.client;

import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.TransportPipeFactory;

/**
 * @author Alexey Stashok
 */
public class JMSTransportPipeFactory extends TransportPipeFactory {
    
    
    public JMSTransportPipeFactory() {
    }
    
    public Pipe doCreate(EndpointAddress address, WSDLPort wsdlModel, WSService service, WSBinding binding) {
        if (address.getURI().getScheme().equalsIgnoreCase("jms")) {
            return new JMSTransportPipe(binding);
        }
        
        return null;
    }
}
