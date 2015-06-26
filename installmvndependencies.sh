#!/bin/bash
echo //// Installing Dependencies. ////
mvn install:install-file -Dfile=lib/axis2-1.6.1.wso2v4.jar -DgroupId=org.apache.axis2.wso2 -DartifactId=axis2 -Dversion=1.6.1.wso2v4 -Dpackaging=jar ;
mvn install:install-file -Dfile=lib/axiom-1.2.11.wso2v1.jar -DgroupId=org.apache.axiom -DartifactId=axiom -Dversion=1.2.11.wso2v1 -Dpackaging=jar ;
mvn install:install-file -Dfile=lib/org.wso2.securevault-1.0.0.jar -DgroupId=org.wso2.securevault -DartifactId=securevault -Dversion=1.0.0 -Dpackaging=jar ;
mvn install:install-file -Dfile=lib/EventStreamAdminService-test-client-1.0.0.jar -DgroupId=org.wso2.carbon.event.stream.admin -DartifactId=EventStreamAdminService -Dversion=1.0.0 -Dpackaging=jar ;
mvn install:install-file -Dfile=lib/EventStreamPersistenceAdminService-test-client-1.0.0.jar -DgroupId=org.wso2.carbon.analytics.stream.persistence -DartifactId=EventStreamPersistenceAdminService -Dversion=1.0.0 -Dpackaging=jar ;
mvn install:install-file -Dfile=lib/EventReceiverAdminService-test-client-1.0.0.jar -DgroupId=org.wso2.carbon.event.receiver.admin -DartifactId=EventReceiverAdminService -Dversion=1.0.0 -Dpackaging=jar ;

