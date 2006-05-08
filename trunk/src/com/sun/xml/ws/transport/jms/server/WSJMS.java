/*
 * WSJMS.java
 */

package com.sun.xml.ws.transport.jms.server;

import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.jms.JMSConstants;
import com.sun.xml.ws.transport.jms.JMSURI;
import java.io.IOException;
import java.io.InputStream;
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
        InputStream is = context.getResourceAsStream(JAXWS_RI_RUNTIME);
        
        try {
            return parser.parse(is);
        } finally {
            is.close();
        }
    }
    
    private Connection initializeJMSConnection(String host, int port, String factoryName, String queueName) throws Exception {
        Connection connection = null;
        
        Properties env = new Properties();
        env.put("org.omg.CORBA.ORBInitialHost", host);
        env.put("org.omg.CORBA.ORBInitialPort", String.valueOf(port));
        
        try {
            InitialContext ic = new InitialContext(env);
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
