package com.sun.xml.ws.transport.jms.client;

import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.istack.NotNull;

/**
 * This is the entry point to the JMS transport extension.
 * 
 * @author Kohsuke Kawaguchi
 */
public class JMSTransportTubeFactory extends TransportTubeFactory {

    public Tube doCreate(@NotNull ClientTubeAssemblerContext context) {
        if (context.getAddress().getURI().getScheme().equalsIgnoreCase("x-jms")) {
            return new JMSTransportTube(context.getCodec());
        }

        return null;

    }
}
