package com.sun.xml.ws.transport.jms.binding;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

@WebService
@BindingType(JmsBindingId.SOAP11JMS_BINDING)
public class JmsBindingIdTestService {
	
	@Oneway
	public void sendMessage(@WebParam(name="message") String m) {
		System.out.println(m);
	}

}
