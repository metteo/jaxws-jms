#!/bin/bash
$AS_HOME/bin/asadmin delete-jms-resource fromwsdlsoap12Q
$AS_HOME/bin/asadmin delete-jms-resource jaxwsfactory
$AS_HOME/bin/asadmin delete-jmsdest -T queue sampleQueue
