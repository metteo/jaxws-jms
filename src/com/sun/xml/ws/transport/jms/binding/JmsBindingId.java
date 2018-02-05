package com.sun.xml.ws.transport.jms.binding;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;

//https://www.w3.org/TR/soapjms/
public class JmsBindingId extends BindingID {

	/**
	 * A constant representing the identity of the SOAP 1.1 over JMS binding.
	 */
	public static final String SOAP11JMS_BINDING = "http://www.w3.org/2010/soapjms/soap1.1";
	
	public static final JmsBindingId SOAP11_JMS = new JmsBindingId(SOAPVersion.SOAP_11, SOAP11JMS_BINDING);

	/**
	 * A constant representing the identity of the SOAP 1.2 over JMS binding.
	 */
	public static final String SOAP12JMS_BINDING = "http://www.w3.org/2010/soapjms/soap1.2";
	
	public static final JmsBindingId SOAP12_JMS = new JmsBindingId(SOAPVersion.SOAP_12, SOAP12JMS_BINDING);

	private static final String TRANSPORT_JMS = "http://www.w3.org/2010/soapjms/";

	private final SOAPVersion soapVersion;
	private final String lexical;

	private JmsBindingId(SOAPVersion soapVersion, String lexical) {
		this.soapVersion = soapVersion;
		this.lexical = lexical;
	}

	public SOAPVersion getSOAPVersion() {
		return soapVersion;
	}

	public Codec createEncoder(WSBinding binding) {
		return Codecs.createSOAPBindingCodec(binding, Codecs.createSOAPEnvelopeXmlCodec(binding.getSOAPVersion()));
	}

	@Override
	public String getTransport() {
		return TRANSPORT_JMS;
	}

	public String toString() {
		return lexical;
	}

	@Override
	public boolean canGenerateWSDL() {
		return true;
	}
}
