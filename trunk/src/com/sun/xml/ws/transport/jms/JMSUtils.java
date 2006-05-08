/*
 * JMSUtils.java
 */

package com.sun.xml.ws.transport.jms;

/**
 * @author Alexey Stashok
 */
public class JMSUtils {
    public static boolean isStatusError(int status) {
        return status >= JMSConstants.ERROR_STATUS_LIMIT;
    }
}
