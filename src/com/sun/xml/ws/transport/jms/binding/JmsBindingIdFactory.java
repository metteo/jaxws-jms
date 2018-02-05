package com.sun.xml.ws.transport.jms.binding;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.BindingIDFactory;

public class JmsBindingIdFactory extends BindingIDFactory {

	@Override
	public BindingID parse(String lexical) throws WebServiceException {
		if (lexical == null) { return null; }
		
		switch (lexical) {
			case JmsBindingId.SOAP11JMS_BINDING: return JmsBindingId.SOAP11_JMS;
			case JmsBindingId.SOAP12JMS_BINDING: return JmsBindingId.SOAP12_JMS;
		}
		
		return null;
	}

}
