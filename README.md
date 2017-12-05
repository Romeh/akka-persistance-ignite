![Build Status](https://travis-ci.org/Romeh/akka-persistance-ignite.svg?branch=master)
# akka-persistence-ignite (Java API)
A _**journal**_ and _**snapshot**_  store plugin for akka-persistence using **_Apache Ignite_**.

![alt text](Final.png)

This is mainly using Apache ignite with akka persistence to provide journal and snapshot store 
by using the partitioned caches and benefit from the distributed highly available data grid features plus
the nice query and data computations features in Ignite that can be used to have normalized views from the event store and
do analytical jobs over them despite it is advised to keep write nodes separate from read nodes for better scalability

Akka and Ignite used versions:
--------------

Akka version :2.5.7+ , Ignite Version :2.3.0+

Journal plugin 
--------------
- All operations required by the Akka Persistence journal plugin API are fully supported.
- It use apache ignite partitioned cache with default number of backups to 1 , that cna be changed into reference.conf file.
 
Snapshot store plugin (Akka version :2.5.7+ , Ignite Version :2.3.0+)
---------------------
 - Implements the Akka Persistence [snapshot store plugin API](https://doc.akka.io/docs/akka/current/scala/persistence.html#snapshot-store-plugin-api).
 
  
How to use 
---------------------
Enable plugins
````buildoutcfg
akka.persistence.journal.plugin = "akka.persistence.journal.ignite"
akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot.ignite"
````

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
- [Persistence Query](https://doc.akka.io/docs/akka/current/scala/persistence-query.html) to BE IMPLEMENTED
- More Detailed tests under failure conditions are still missing.
- Performance testing policy needed to be defined and executed 
- Adding HTTPS support 
- Performance test ignite client node vs server node connectivity
