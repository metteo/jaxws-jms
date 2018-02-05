package com.sun.xml.ws.transport.jms.binding;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.xml.ws.Endpoint;

import org.junit.Before;
import org.junit.Test;

import com.sun.xml.ws.api.BindingID;

public class JmsBindingIdFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testParseString() {
		//given
		
		//when
		BindingID b = BindingID.parse(JmsBindingId.SOAP11JMS_BINDING);
		
		//then
		assertEquals(b, (JmsBindingId.SOAP11_JMS));
	}
	
	@Test
    public void testRun() throws Exception {
        // publish my service
        int port = new Random().nextInt(10000)+10000;
        String address = "http://localhost:" + port + "/book";
        Endpoint endpoint = Endpoint.publish(address, new JmsBindingIdTestService());

        hitEndpoint(new URL(address));
        endpoint.stop();
    }

    private void hitEndpoint(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url+"?wsdl").openConnection();
        dump(con);
    }

    private void dump(HttpURLConnection con) throws IOException {
        // Check if we got the correct HTTP response code
        int code = con.getResponseCode();

        // Check if we got the correct response
        InputStream in = (code == 200) ? con.getInputStream() : con.getErrorStream();
        int ch;
        while((ch=in.read()) != -1) {
            System.out.print((char)ch);
        }
        in.close();
    }

}
