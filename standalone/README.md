Infinispan standalone application with embedded cache and Partition Handling
=============================================================================
Author: Wolf-Dieter Fink
Level: Adcanced
Technologies: Infinispan


What is it?
-----------

Show how an application can create an clustered Infinispan cache in library mode configured with XML files.
The configuration will use partition handling and a custom merge policy based on the creation date of entries.

It is used to show the behaviour of partition handling and the custom implementation of a merger in detail.

Build and Run
-------------
1. Type this command to build and run the application:

        mvn clean install exec:java

2. To run the application with different UDP settings set MAVEN_OPTS to run the exec command
   This is helpful if you run instances on different machines as the default bind address is localhost

       MAVEN_OPTS="-Dinfinispan.cache.bind_addr=<your IP address>" mvn exec:java [-Dexec.args="-L 3099 -F my.log -C"]

   For available properties, or to add some, see src/main/resources/.xml files

3. Start several instances and disconnect network or suspend the JVM to see how the cache behave during split and on merge

   - suspend a JVM for only one node will be detected, as the suspended node will not change the cluster-view.
     in this case the node will clear the caches and start a full rebalance

4. Some further experiments to understand partition handling

   - change partition handling merge-policy to the available policies NONE, PREFERRED_ALWAYS, PREFERRED_NON_NULL, REMOVE_ALL
   - change to when-split to DENY_READ_WRITES, ALLOW_READS



With the arguments the logging can be adjusted
 -L abcd where abcd is 0....9 and represent the log level FATAL.....ALL
    a    org.jgroups  default 3
     b   org.infinispan default 0
      c  org.infinispan.<topology and merge>  default 9
       d example classes (custom merge policy) default 9
 -F a file name for logging, in this case the console will be limited to INFO level
 -C supress console logging
