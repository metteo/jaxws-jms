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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.transport.jms.JMSConstants;

import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public class JMSTransportTube extends AbstractTubeImpl {
    private final Codec codec;
    
    public JMSTransportTube(Codec codec) {
        this.codec = codec;
    }
    
    protected JMSTransportTube(JMSTransportTube that, TubeCloner cloner) {
        super(that,cloner);
        this.codec = that.codec.copy();
    }
    
    public void preDestroy() {
    }

    public JMSTransportTube copy(TubeCloner cloner) {
        return new JMSTransportTube(this,cloner);
    }


    @NotNull
    public NextAction processRequest(@NotNull Packet request) {
        ByteArrayInputStream replyPacketInStream = null;
        ByteArrayOutputStream requestPacketOutStream = null;

        try {
            // get transport headers from message
            Map<String, String> reqHeaders = (Map<String, String>) request.invocationProperties.get(JMSConstants.JMS_REQUEST_HEADERS);
            //assign empty map if its null
            if(reqHeaders == null){
                reqHeaders = new HashMap<String, String>();
            }

            JMSClientTransport con = new JMSClientTransport(request, reqHeaders);

            ContentType ct = codec.getStaticContentType(request);
            requestPacketOutStream = new ByteArrayOutputStream();
            ContentType dynamicCT = codec.encode(request, requestPacketOutStream);
            if (ct == null) {
                // data size is available, set it as Content-Length
                ct = dynamicCT;
            }

            reqHeaders.put(JMSConstants.CONTENT_TYPE_PROPERTY, ct.getContentType());
            reqHeaders.put(JMSConstants.TARGET_URI_PROPERTY, request.endpointAddress.getURI().toASCIIString());

            byte[] rplPacket = con.sendMessage(requestPacketOutStream.toByteArray());

            if(!con.isPayloadExist()) {
                return doReturnWith(request.createClientResponse(null));    // one way. null response given.
            }

            Map<String, String> respHeaders = con.getHeaders();
            String contentTypeStr = getContentType(respHeaders);

            Packet reply = request.createClientResponse(null);
            replyPacketInStream = new ByteArrayInputStream(rplPacket);
            codec.decode(replyPacketInStream, contentTypeStr, reply);
            return doReturnWith(reply);
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


    @NotNull
    public NextAction processResponse(@NotNull Packet response) {
        throw new AssertionError();
    }

    @NotNull
    public NextAction processException(@NotNull Throwable t) {
        throw new AssertionError();
    }

    private String getContentType(Map<String, String> headers) {
        String key = headers.get(JMSConstants.CONTENT_TYPE_PROPERTY);
        if (key == null) {
            throw new WebServiceException("No Content-Type in the header!");
        }
        return key;
    }
}
