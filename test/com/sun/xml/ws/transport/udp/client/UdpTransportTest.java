package com.sun.xml.ws.transport.udp.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;

import org.junit.Before;
import org.junit.Test;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;

public class UdpTransportTest {

	@Before
	public void setUp() throws Exception {
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
	}

	@Test
	public void test() throws Exception {
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				if(protocol.equals("soap.udp")) {
					return new URLStreamHandler() {
						
						@Override
						protected URLConnection openConnection(URL u) throws IOException {
							return new URLConnection(u) {
								
								@Override
								public void connect() throws IOException {
									System.out.println("Connected");
								}
							};
						}
					};
				}
				return null;
			}
		});
		
		URL url = new URL("http://localhost:8080/udp");
		
		//TODO: UdpEndpoint.publish(...) which does http publish as well
		Endpoint e = Endpoint.publish(url.toString(), new SampleUdp_1_0Impl());
		
		Service s = Service.create(url, new QName("http://abc", "SampleUdp_1_0"));
		
		OneWayFeature onewayfeature = new OneWayFeature(true, new WSEndpointReference("soap.udp://localhost:10001/udp✓", AddressingVersion.W3C));
		SampleUdp_1_0 p = s.getPort(SampleUdp_1_0.class, new AddressingFeature(true), onewayfeature);
		
		BindingProvider bp = (BindingProvider) p;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "soap.udp://239.255.255.250:3702/udp");
		
		p.sendMessage("abc");
		
		String ss = p.sendMessage_2_0("cde");
		System.out.println(ss);
		
		e.stop();
	}

}
