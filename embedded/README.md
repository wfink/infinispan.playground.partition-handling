Infinispan application with embedded cache: Use of Library mode and EAP modules
===============================================================================
Author: Wolf-Dieter Fink
Level: Adcanced
Technologies: Infinispan, Web application


What is it?
-----------

Show how a web application can create a Infinispan cache in library mode configured with XML files.

Build and Run
-------------
1. Type this command to build and deploy the archive:

        mvn clean package

2. Install the Infinispan/JDG modules to WildFly or EAP server and deploy the application

       unzip jboss-datagrid-7.3.0-1-eap-modules.zip 
       cp -a jboss-datagrid-7.3.0-eap-modules/modules/* SERVER/modules
       cp web/target/embeddedCacheWithPartitionHandling.war SERVER/standalone/deployments

3. Start a WildFly or EAP server

   Note: the WildFly/EAP server does not need to have a HA profile as Infinispan/RHDG will use it's own configuration!

   Option 1 (UDP multicast)

       bin/standalone.sh  -Djboss.socket.binding.port-offset=0 -Dinfinispan.cache.bind_addr=<my IP>
   
   Option 2 (TCP PING) need to change the infinispan configuration

       bin/standalone.sh  -Djboss.socket.binding.port-offset=0 -Dinfinispan.cache.bind_addr=<my IP> -Dinfinispan.tcpping.initial_hosts="<my IP>[<my port>],<other IP>[<other port>]" -Dinfinispan.tcp.bind_port=<my port>


   Use the port-offset if you start more than one server on the same machine to separate the EAP instances
   use infinispan.cache.bind_addr [localhost] to identify your JDG instance
   use infinispan.tcpping.initial_hosts [localhost[7800],localhost[7801]] to add ALL servers in your JDG cluster
   use infinispan.tcp.bind_port [7800] to separate the JDG instances if you start more than one on a machine

4. Open a Browser and access the application

      http://localhost:8080/embeddedCacheWithPartitionHandling

