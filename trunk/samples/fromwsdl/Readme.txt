fromwsdl sample demonstrates the WSDL->Java programming model.

* It has two operations with different MEPs
    * in/out - addNumbers()
    * oneway - onewayInt()
* etc - configuration files
    * AddNumbers.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment WS descriptor
    * /META-INF/services/com.sun.xml.ws.api.pipe.TransportPipeFactory transport pipe config file
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService
      and exception class - AddNumbersFault_Exception

* To run
    * using GlassFish admin console create following JMS artifacts:
      JMS Factory: jaxwsfactory
      JMS Queue: fromwsdlQ
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