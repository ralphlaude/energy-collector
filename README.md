# Vermietet.de - Energy Collector Service

This service provides an REST-API for collecting counter energy for villages.
The service is implemented using the Lagom framework (https://www.lagomframework.com/documentation/1.3.x/java/ReferenceGuide.html).


REST API Operations
-------------------

The service is available here (http://localhost:9000/energycollector)

Format for API operations:

```
1. POST http://localhost:9000/energycollector/counter_callback
    with
    request body {"counterId":1,"villageName":"test","initialAmount":10.12}
    and
    extract the response "Location" header
    
2. POST http://localhost:9000/energycollector/counter_update?id=1    
     with request body {"totalAmount":15.12} 
     
3. GET http://localhost:9000/energycollector/counter?id=1     

4. GET http://localhost:9000/energycollector/consumption_report?duration=24          
         
```

Running the service
-------------------

You should install SBT (0.13.13), Java 8 and Scala (2.11.8) to be able to run the service.

```
*   Unzip vermietetde-energy-collector.zip in local directory
*   Open terminal and go to vermietetde-energy-collector folder
*   Run application with sbt runAll
```

Running test
------------
```
*  Open terminal and go to vermietetde-energy-collector folder
*  sbt test
```

Importing the project to IDE
----------------------------
please see https://www.lagomframework.com/documentation/1.3.x/java/ReferenceGuide.html
for Eclipse please see https://www.lagomframework.com/documentation/1.3.x/java/EclipseSbt.html
for Intellij please see https://www.lagomframework.com/documentation/1.3.x/java/IntellijSbtJava.html
for setting up Immutables please see https://www.lagomframework.com/documentation/1.3.x/java/ImmutablesInIDEs.html