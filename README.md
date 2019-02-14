# akka-persistence-ignite (Java API)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5cb7dec4f09b44eca8677c5feebadcf1)](https://app.codacy.com/app/Romeh/akka-persistance-ignite?utm_source=github.com&utm_medium=referral&utm_content=Romeh/akka-persistance-ignite&utm_campaign=Badge_Grade_Dashboard)

![Build Status](https://travis-ci.org/Romeh/akka-persistance-ignite.svg?branch=master)
![Maven Central](https://img.shields.io/nexus/r/https/oss.sonatype.org/io.github.romeh/akka-persistence-ignite.svg?style=flat)

A _**journal**_ and _**snapshot**_  store plugin for [Akka Persistence](https://doc.akka.io/docs/akka/2.5.4/java/persistence.html) using **_Apache Ignite_**.

![alt text](Final.png)

This is mainly using Apache ignite with akka persistence to provide journal and snapshot store 
by using the partitioned caches and benefit from the distributed highly available data grid features plus
the nice query and data computations features in Ignite that can be used to have normalized views from the event store and
do analytical jobs over them despite it is advised to keep write nodes separate from read nodes for better scalability

Akka and Ignite used versions:
--------------

Akka version :2.5.18+ , Ignite Version :2.7.0+

Journal plugin 
--------------
- All operations required by the Akka Persistence journal plugin API are fully supported.
- it support storing the tags of the events (event tagging)
- It use Apache Ignite binary serialization for its queries
- Events serialization will be based into the types serializers definition in your Akka configuration 
- It use apache ignite partitioned cache with default number of backups to 1 , that can be changed into reference.conf file.
 
Snapshot store plugin
---------------------
 - Implements the Akka Persistence [snapshot store plugin API](https://doc.akka.io/docs/akka/current/scala/persistence.html#snapshot-store-plugin-api).
 
  
How to use 
---------------------
Enable plugins
````buildoutcfg
akka.persistence.journal.plugin = "akka.persistence.journal.ignite"
akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot.ignite"
````

Maven dependency 
````
        <dependency>
            <groupId>io.github.romeh</groupId>
            <artifactId>akka-persistence-ignite</artifactId>
            <version>1.1.1</version>
        </dependency>
````

please check the project pom.xml for maven repo configuration

Configure Ignite data grid properties , default configured on localhost. 
````buildoutcfg
ignite {
  //to start client or server node to connect to Ignite data cluster 
  isClientNode = false
  // for ONLY testing we use localhost
  // used for grid cluster connectivity
  tcpDiscoveryAddresses = "localhost"
  metricsLogFrequency = 0
  // thread pools used by Ignite , should based into target machine specs
  queryThreadPoolSize = 4
  dataStreamerThreadPoolSize = 1
  managementThreadPoolSize = 2
  publicThreadPoolSize = 4
  systemThreadPoolSize = 2
  rebalanceThreadPoolSize = 1
  asyncCallbackPoolSize = 4
  peerClassLoadingEnabled = false
  // to enable or disable durable memory persistance
  enableFilePersistence = true
  // used for grid cluster connectivity, change it to suit your configuration 
  igniteConnectorPort = 11211
  // used for grid cluster connectivity , change it to suit your configuration 
  igniteServerPortRange = "47500..47509"
  //durable memory persistance storage file system path , change it to suit your configuration 
  ignitePersistenceFilePath = "./data"
}
````
Contributions are more then welcomed:
---------------------
Please free to jump and help will be highly appreciated 

TODO (enhancement issues created for tracking): 
---------------------
- [Persistence Query](https://doc.akka.io/docs/akka/current/scala/persistence-query.html) to be implemented but it could be not needed as we can use the Apache Ignite sql and text query capabilities 
- Performance testing policy needed to be defined and executed 
- Adding HTTPS support 
- Performance test ignite client node vs server node connectivity
