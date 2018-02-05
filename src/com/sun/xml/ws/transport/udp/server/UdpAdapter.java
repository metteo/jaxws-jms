package com.sun.xml.ws.transport.udp.server;

import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.WSEndpoint;

public class UdpAdapter extends Adapter<UdpAdapter.UdpToolkit>{
	
	
	protected UdpAdapter(WSEndpoint endpoint) {
		super(endpoint);
	}

	class UdpToolkit extends Adapter<UdpToolkit>.Toolkit {
		
	}

	@Override
	protected UdpToolkit createToolkit() {
		return new UdpToolkit();
	}

}
