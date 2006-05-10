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

package com.sun.xml.ws.transport.jms;

import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Alexey Stashok
 */
public class JMSConstants {
    public static final String JMS_ONE_WAY_PROPERTY_NAME = "ONE_WAY";
    public static final String CONTENT_TYPE_PROPERTY = "CONTENT_TYPE";

    public static final int ERROR_STATUS_LIMIT = 400;
    public static final int ONEWAY = WSHTTPConnection.ONEWAY;
    public static final int ERROR_INTERNAL = 500;
    
    public static final String TARGET_URI_PROPERTY = "TARGET_URI";
    public static final String REPLY_STATUS_PROPERTY = "REPLY_STATUS";
    public static final String SOAP_ACTION_PROPERTY = "SOAPAction";

    public static final String TARGET_SERVICE_URI = "path";

    public static final String JMS_REQUEST_HEADERS = "JMS-RQ-Headers";
    public static final String JMS_RESPONSE_HEADERS = "JMS-RSP-Headers";
}
