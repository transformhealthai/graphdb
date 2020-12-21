## Getting Started
--------------------

Go to project's root directory, `cd <your project name>`

*Command for providing arguments to OpenCsvParser* 
--------------------------------------------------
mvn clean compile exec:java -Dexec.mainClass="com.thoughti.neo4j.Neo4jCsvParser" -DcsvFilePath="C:/transformhealth/projectDocs/" -DsourceFileName="HD Deposits.csv" -DmappingFileName="HD"


*Command for providing arguments to Neo4jFetch*
---------------------------------------------------
mvn clean compile exec:java -Dexec.mainClass="com.thoughti.neo4j.Neo4jFetch" -DmappingFileName="HD"