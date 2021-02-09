## Getting Started
--------------------

1) OpenCsvParser: This class parse the data from csv file input and uploads to neo4j.
2) Neo4jFetch: This class includes methods to fetch defineFile,records,definefields data from graphdb and post it to elasticsearch.
3) Neo4jFetchFileInstanceData: This class includes method for fetching fileInstance,sectioninstance data from graphdb and post it to elasticsearch.
4) ElasticSearchPost: This class includes methods for creating elasticsearch session and uploading data on elasticsearch. 
5) Neo4jsession: This class includes method for creating neo4j garphdb session.


*Command for OpenCsvParser* 
--------------------------------------------------
mvn clean compile exec:java -Dexec.mainClass="com.thoughti.neo4j.Neo4jCsvParser" -DcsvFilePath="C:/transformhealth/projectDocs/" -DsourceFileName="HD Deposits.csv" -DmappingFileName="HD"


*Command for Neo4jFetch*
---------------------------------------------------
mvn clean compile exec:java -Dexec.mainClass="com.thoughti.neo4j.Neo4jFetch" -DmappingFileName="HD"


*Command for Neo4jFetchFileInstanceData*
-------------------------------------------------------------------
mvn clean compile exec:java -Dexec.mainClass="com.thoughti.neo4j.Neo4jFetchFileInstanceData"


