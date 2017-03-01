#!/bin/bash
$AS_HOME/bin/asadmin create-jmsdest -T queue sampleQueue
$AS_HOME/bin/asadmin create-jms-resource --restype javax.jms.ConnectionFactory jaxwsfactory
$AS_HOME/bin/asadmin create-jms-resource --restype javax.jms.Queue --property Name=sampleQueue fromwsdlsoap12Q
