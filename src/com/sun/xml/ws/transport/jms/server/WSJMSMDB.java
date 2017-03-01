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
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;

/**
 * @author Alexey Stashok
 */
public class WSJMSMDB implements MessageListener, MessageDrivenBean {
    private transient MessageDrivenContext mdc;
    private transient ConnectionFactory connectionFactory;
    
    private transient WSJMSDelegate delegate;
    
    private static final String JAXWS_RI_RUNTIME = "WEB-INF/sun-jaxws.xml";
    
    public void ejbCreate() {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            JMSContext context = new JMSStandaloneContext(new InitialContext(), classloader);

            List<JMSAdapter> adapters = parseDeploymentDescriptor(context, classloader);
            delegate = new WSJMSDelegate(adapters, context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void ejbRemove() throws EJBException {
    }
    
    public void onMessage(Message message) {
        if (message instanceof BytesMessage) {
            try {
                delegate.process((BytesMessage) message);
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static List<JMSAdapter> parseDeploymentDescriptor(JMSContext context, ClassLoader classloader) throws IOException {
        DeploymentDescriptorParser<JMSAdapter> parser = new DeploymentDescriptorParser<JMSAdapter>(
                classloader, new JMSResourceLoader(context), null, JMSAdapter.FACTORY);
        URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
        return parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
    }
    
    public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) throws EJBException {
        mdc = messageDrivenContext;
    }
    
}
