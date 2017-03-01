#!/bin/bash
$AS_HOME/bin/asadmin delete-jms-resource fromwsdlQ
$AS_HOME/bin/asadmin delete-jms-resource jaxwsfactory
$AS_HOME/bin/asadmin delete-jmsdest -T queue sampleQueue
