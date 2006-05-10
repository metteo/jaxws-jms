/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.jms.client;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Decoder;
import com.sun.xml.ws.api.pipe.Encoder;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.transport.jms.JMSConstants;

import javax.xml.ws.WebServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexey Stashok
 */
public class JMSTransportPipe implements Pipe {
    private final Encoder encoder;
    private final Decoder decoder;
    
    public JMSTransportPipe(WSBinding binding) {
        this(binding.createEncoder(),binding.createDecoder());
    }
    
    private JMSTransportPipe(Encoder encoder, Decoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }
    
    private JMSTransportPipe(JMSTransportPipe that, PipeCloner cloner) {
        this(that.encoder, that.decoder);
        cloner.add(that, this);
    }
    
    public void preDestroy() {
    }
    
    public Pipe copy(PipeCloner cloner) {
        return new JMSTransportPipe(this, cloner);
    }
    
    public Packet process(Packet packet) {
        ByteArrayInputStream replyPacketInStream = null;
        ByteArrayOutputStream requestPacketOutStream = null;
        
        try {
            // get transport headers from message
            Map<String, String> reqHeaders = (Map<String, String>) packet.invocationProperties.get(JMSConstants.JMS_REQUEST_HEADERS);
            //assign empty map if its null
            if(reqHeaders == null){
                reqHeaders = new HashMap<String, String>();
            }
            
            JMSClientTransport con = new JMSClientTransport(packet, reqHeaders);
            
            ContentType ct = encoder.getStaticContentType(packet);
            requestPacketOutStream = new ByteArrayOutputStream();
            ContentType dynamicCT = encoder.encode(packet, requestPacketOutStream);
            if (ct == null) {
                // data size is available, set it as Content-Length
                ct = dynamicCT;
            }
            
            reqHeaders.put(JMSConstants.SOAP_ACTION_PROPERTY, ct.getSOAPAction());
            reqHeaders.put(JMSConstants.CONTENT_TYPE_PROPERTY, ct.getContentType());
            reqHeaders.put(JMSConstants.TARGET_URI_PROPERTY, packet.endpointAddress.getURI().toASCIIString());
            
            byte[] rplPacket = con.sendMessage(requestPacketOutStream.toByteArray());
            
            if(!con.isPayloadExist()) {
                return packet.createResponse(null);    // one way. null response given.
            }
            
            Map<String, String> respHeaders = con.getHeaders();
            String contentTypeStr = getContentType(respHeaders);
            
            Packet reply = packet.createResponse(null);
            replyPacketInStream = new ByteArrayInputStream(rplPacket);
            decoder.decode(replyPacketInStream, contentTypeStr, reply);
            return reply;
        } catch(WebServiceException wex) {
            throw wex;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new WebServiceException(ex);
        } finally {
            if (requestPacketOutStream != null) {
                try {
                    requestPacketOutStream.close();
                } catch (IOException ex) {
                }
            }
            
            if (replyPacketInStream != null) {
                try {
                    replyPacketInStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    
    private String getContentType(Map<String, String> headers) {
        String key = headers.get(JMSConstants.CONTENT_TYPE_PROPERTY);
        if (key == null) {
            throw new WebServiceException("No Content-Type in the header!");
        }
        return key;
    }
}
