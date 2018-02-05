package com.sun.xml.ws.transport.udp.client;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

@Addressing
@WebService(targetNamespace="http://abc")
public interface SampleUdp_1_0 {

	@Oneway
	public void sendMessage(@WebParam(name="message") String m);
	
	@WebResult(name="message")
	public String sendMessage_2_0(@WebParam(name="message") String m);
}
