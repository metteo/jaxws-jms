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

package com.sun.xml.ws.transport.jms.server;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;

import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.jms.JMSConstants;
import com.sun.xml.ws.transport.jms.JMSURI;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.JMSException;

/**
 * @author Alexey Stashok
 */
public class JMSAdapter extends Adapter<JMSAdapter.JMSToolkit> {
    final String name;
    final String urlPattern;
    
    public JMSAdapter(String name, String urlPattern, WSEndpoint endpoint) {
        super(endpoint);
        this.name = name;
        this.urlPattern = urlPattern;
    }
    
    public void handle(JMSContext context, BytesMessage requestMessage,
                       JMSURI uri) throws IOException, JMSException {
        JMSConnectionImpl connection = new JMSConnectionImpl(context, requestMessage, uri);
        
        JMSToolkit tk = pool.take();
        try {
            tk.handle(connection);
            connection.flush();
        } finally {
            pool.recycle(tk);
            connection.close();
        }
    }
    
    protected JMSToolkit createToolkit() {
        return new JMSToolkit();
    }
    
    /**
     * Returns the "/abc/def/ghi" portion if
     * the URL pattern is "/abc/def/ghi/*".
     */
    public String getValidPath() {
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0, urlPattern.length() - 2);
        } else {
            return urlPattern;
        }
    }
    
    class JMSToolkit extends Adapter.Toolkit implements TransportBackChannel {
        private JMSConnectionImpl connection;
        
        private void handle(JMSConnectionImpl connection) throws IOException {
            this.connection = connection;
            
            String contentTypeStr = connection.getRequestHeader(JMSConstants.CONTENT_TYPE_PROPERTY);
            InputStream in = connection.getInputStream();
            Packet packet = new Packet();
            codec.decode(in, contentTypeStr, packet);
            try {
                packet = head.process(packet, connection, this);
            } catch(Exception e) {
                e.printStackTrace();
                writeInternalServerError(connection);
                return;
            }
            
            contentTypeStr = codec.getStaticContentType(packet).getContentType();
            if (contentTypeStr == null) {
                throw new UnsupportedOperationException();
            } else {
                Map<String, String> headers = new HashMap();
                headers.put(JMSConstants.CONTENT_TYPE_PROPERTY, contentTypeStr);
                connection.setResponseHeaders(headers);
                if (packet.getMessage() == null) {
                    connection.setStatus(JMSConstants.ONEWAY);
                } else {
                    codec.encode(packet, connection.getOutputStream());
                }
            }
            
        }
        
        private void writeInternalServerError(JMSConnectionImpl connection) {
            connection.setStatus(JMSConstants.ERROR_INTERNAL);
        }
        
        public void close() {
            connection.setStatus(JMSConstants.ONEWAY);
            connection.close();
        }
    };
    
    public static final AdapterFactory<JMSAdapter> FACTORY = new AdapterFactory<JMSAdapter>() {
        public JMSAdapter createAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
            String portURI = endpoint.getPort().getAddress().getURI().toASCIIString();
            JMSURI jmsURI = JMSURI.parse(portURI);
            String path = jmsURI.getParameter(JMSConstants.TARGET_SERVICE_URI);
            
            return new JMSAdapter(name, path, endpoint);
        }
    };
}
