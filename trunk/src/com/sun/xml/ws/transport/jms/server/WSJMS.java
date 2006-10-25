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

import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.jms.JMSURI;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 *
 * @author Alexey Stashok
 */
public class WSJMS implements MessageListener {
    
    private static final String JAXWS_RI_RUNTIME = "WEB-INF/sun-jaxws.xml";
    
    private JMSContext context;
    private WSJMSDelegate delegate;
    
    public List<JMSAdapter> parseDeploymentDescriptor() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        context = new JMSStandaloneContext(classloader);
        
        DeploymentDescriptorParser<JMSAdapter> parser = new DeploymentDescriptorParser<JMSAdapter>(
                classloader, new JMSResourceLoader(context), null, JMSAdapter.FACTORY);
        URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
        return parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
    }
    
    private Connection initializeJMSConnection(String host, int port, String factoryName, String queueName) throws Exception {
        Connection connection = null;
        
        Properties props = new Properties();
        props.setProperty("java.naming.factory.initial",
                "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty("java.naming.factory.url.pkgs",
                "com.sun.enterprise.naming");
        props.setProperty("java.naming.factory.state",
                "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        
        props.put("org.omg.CORBA.ORBInitialHost", host);
        props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(port));
        
        try {
            InitialContext ic = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) ic.lookup(factoryName);
            Queue destinationQueue = (Queue) ic.lookup(queueName);
            
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            MessageConsumer consumer = session.createConsumer(destinationQueue);
            
            consumer.setMessageListener(this);
            
            context.setAttribute(JMSContext.SESSION_ATTR, session);
            return connection;
        } catch(Exception e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ee) {
                }
            }
            
            throw e;
        }
    }
    public Connection initialize() {
        try {
            List<JMSAdapter> adapters = parseDeploymentDescriptor();
            delegate = new WSJMSDelegate(adapters, context);
            
            JMSAdapter adapter = adapters.get(0);
            URI uri = adapter.getEndpoint().getPort().getAddress().getURI();
            
            JMSURI jmsURI = JMSURI.parse(uri);
            
            return initializeJMSConnection(jmsURI.host, jmsURI.port, jmsURI.factory, jmsURI.queue);
        } catch (Exception e) {
            throw new WSJMSException("listener.parsingFailed", e);
        }
        
    }
    
    public void process() {
        
        Connection connection = initialize();
        
        try {
            connection.start();
            
            System.out.println("Press enter key to quit");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
        
        System.exit(0);
    }
    
    public void onMessage(Message message) {
        try {
            if (message instanceof BytesMessage) {
                delegate.process((BytesMessage) message);
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        WSJMS wsJMS = new WSJMS();
        wsJMS.process();
    }
    
}
