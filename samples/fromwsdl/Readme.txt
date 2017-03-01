fromwsdl sample demonstrates the WSDL->Java programming model.

* It has two operations with different MEPs
    * in/out - addNumbers()
    * oneway - onewayInt()
* etc - configuration files
    * AddNumbers.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint jar file
    * sun-jaxws.xml deployment WS descriptor
    * /META-INF/services/com.sun.xml.ws.api.pipe.TransportTubeFactory transport pipe config file
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService
      and exception class - AddNumbersFault_Exception

* To run
    * create required JMS artifacts.
      * You can use create-jms-resorces.sh script or
      * using GlassFish admin console create following JMS artifacts:
            JMS Connection Factory: 'jaxwsfactory'
            JMS Queue: 'fromwsdlQ' ,  Physical Destination Name:'sampleQueue'
    * ant server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant client - runs wsimport on the published wsdl by the deployed
      endpoint(now it takes wsdl localy), compiles the generates artifacts
    * ant run-service - runs standalone service server part
    * ant run-client - runs client

Its also possible to deploy WS as EJB module to AppServer (tested just with GlassFish).
To do so you need: 
    * Put JMS transport to AppServer instance lib directory (<AS_HOME>/domains/domain1/lib)
    * Deploy jar file build/jar/jaxws-fromwsdl.jar as EJB module to AppServer
    * Run client

Troubleshooting:

Issue http://java.net/jira/browse/GLASSFISH-4051 can bug you; in that case, modify domain.xml so that the orb-listener is not at 0.0.0.0 but at the IP address the client sees. 

If the client doesn't canoot resolve the hostname of the glassfish server, add this hostname to your hosts file.

If you run this example on a glassfish version other than v2.1.1, you may have to use the jar files from that glassfish installation instead of the ones in ..\lib.
