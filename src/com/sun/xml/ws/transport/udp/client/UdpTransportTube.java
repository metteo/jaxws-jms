package com.sun.xml.ws.transport.udp.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.util.ByteArrayBuffer;

public class UdpTransportTube  extends AbstractTubeImpl {
	
	private static final Logger sLogger = Logger.getLogger("UdpTransportTube");
	
	private Codec codec;
	private WSBinding binding;
	
	public UdpTransportTube(Codec codec, WSBinding binding) {
		this.codec = codec;
		this.binding = binding;
	}
	
    /*
     * Copy constructor for {@link Tube#copy(TubeCloner)}.
     */
    private UdpTransportTube(UdpTransportTube that, TubeCloner cloner) {
        this(that.codec.copy(), that.binding);
        cloner.add(that,this);
    }

	@Override
	public NextAction processRequest(Packet request) {
		return doReturnWith(process(request));
	}

	@Override
	public NextAction processResponse(Packet response) {
		return doReturnWith(process(response));
	}

	@Override
	public NextAction processException(Throwable t) {
		return doThrow(t);
	}
	
	@Override
	public Packet process(Packet p) {
		try {
			return process0(p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Packet process0(Packet request) throws Exception {
		UdpClientTransport con = new UdpClientTransport();
		
		DatagramSocket ds = new DatagramSocket();
		
		
		
        ContentType ct = codec.getStaticContentType(request); //not useful, we don't set any headers like http does
        
    	ByteArrayBuffer buf = new ByteArrayBuffer();
    	ContentType dct = codec.encode(request, buf);
    	if(ct == null) { ct = dct; }
        
        dump(buf, "UDP request (" + buf.size() + " octets) - " + request.endpointAddress);
        URI url = request.endpointAddress.getURI();
        
        InetAddress addr = InetAddress.getByName(url.getHost());
        
		DatagramPacket dp = new DatagramPacket(buf.getRawData(), buf.size(), addr, url.getPort());
		
		ds.send(dp);
		
		Packet reply = request.createClientResponse(null);
		
		if (request.expectReply) {
			Integer reqTimeout = (Integer) request.invocationProperties.get(JAXWSProperties.REQUEST_TIMEOUT);
			if(reqTimeout != null) {
				ds.setSoTimeout(reqTimeout);
			}
			
			DatagramPacket receive = new DatagramPacket(new byte[4096], 4096);
			ds.receive(receive);
			
			ByteArrayBuffer buf2 = new ByteArrayBuffer(receive.getData(), receive.getLength());
			
			dump(buf2, "UDP response (" + buf2.size() + " octets) - " + receive.getAddress().getHostAddress());
			
			codec.decode(new ByteArrayInputStream(buf2.getRawData()), "application/soap+xml", reply);
		}
		
		ds.close();
		
		return reply;
	}

	@Override
	public void preDestroy() {
		// nothing to do
		
	}

	@Override
	public AbstractTubeImpl copy(TubeCloner cloner) {
		return new UdpTransportTube(this, cloner);
	}
	
    private void dump(ByteArrayBuffer buf, String caption) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos, true);
        pw.println("---["+caption +"]---");

        if (buf.size() > HttpAdapter.dump_threshold) {
            byte[] b = buf.getRawData();
            baos.write(b, 0, HttpAdapter.dump_threshold);
            pw.println();
            pw.println(WsservletMessages.MESSAGE_TOO_LONG(HttpAdapter.class.getName() + ".dumpTreshold"));
        } else {
            buf.writeTo(baos);
        }
        pw.println("--------------------");

        String msg = baos.toString();
        //if (dump) {
            //System.out.println(msg);
        //}
        //if (LOGGER.isLoggable(Level.FINER)) {
            sLogger.log(Level.INFO, msg);
        //}
    }
}
