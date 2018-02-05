package com.sun.xml.ws.transport.udp.client;

import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;

public class UdpTransportTubeFactory extends TransportTubeFactory {

	@Override
	public Tube doCreate(ClientTubeAssemblerContext context) {
        if ("soap.udp".equalsIgnoreCase(context.getAddress().getURI().getScheme())) {
            return new UdpTransportTube(context.getCodec(), context.getBinding());
        }

        return null;
	}
}
