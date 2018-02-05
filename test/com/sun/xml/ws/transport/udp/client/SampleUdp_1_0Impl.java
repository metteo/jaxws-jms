package com.sun.xml.ws.transport.udp.client;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;

@Addressing
@WebService(endpointInterface = "com.sun.xml.ws.transport.udp.client.SampleUdp_1_0", targetNamespace="http://abc", serviceName="SampleUdp_1_0")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class SampleUdp_1_0Impl implements SampleUdp_1_0 {

	@Override
	public void sendMessage(String m) {
		System.out.println(m);
	}
	
	@Override
	public String sendMessage_2_0(String m) {
		return ">" + m + "<";
	}

}
