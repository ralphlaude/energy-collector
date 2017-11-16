# Vermietet.de - Energy Collector Service

This service provides an REST-API for collecting counter energy for villages.
The service is implemented using the Lagom framework. Lagom is a microservices service framework.
The service is available here (http://localhost:9000/energycollector)


REST API Operations
-------------------

Format for API operations

```
1. POST http://localhost:9000/energycollector/counter_callback
    with
    request body {"counterId":1,"villageName":"test","initialAmount":10.12}
    and
    extract the response "Location" header
    
2. POST http://localhost:9000/energycollector/counter_update?id=1    
     with request body {"totalAmount":15.12} 
     
3. GET http://localhost:9000/energycollector/counter?id=1     

4. GET http://localhost:9000/energycollector/consumption_report?duration=12          
         
```

Running the service
-------------------

You should install SBT (0.13.13), Java 8 and Scala (2.11.8) to be able to run the service.

```
*   Unzip vermietetde-energy-collector.zip in local directory
*   Open terminal and go to vermietetde-energy-collector folder
*   Run application with sbt runAll
```

Importing the project to IDE
----------------------------
Please see http://www.lagomframework.com/documentation/1.2.x/java/IDEs.html

```
*   sbt test
```