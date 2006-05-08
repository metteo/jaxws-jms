/*
 * Constants.java
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

    public static final String TARGET_SERVICE_URI = "path";

    public static final String JMS_REQUEST_HEADERS = "JMS-RQ-Headers";
    public static final String JMS_RESPONSE_HEADERS = "JMS-RSP-Headers";
}
