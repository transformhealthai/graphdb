package com.thoughti.neo4j;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Neo4jCsvParser {
	public static void main(String[] args) {
		
		CSVReader reader = null;
		try {
			// Passing values from 
			String csvFilePath = System.getProperty("csvFilePath");
			String sourceFileName = System.getProperty("sourceFileName");
			String mappingFileName = System.getProperty("mappingFileName");

			// Create File entry in database
			String fileCreateQuery = "CREATE (file:File { fileName:\"" + mappingFileName+".csv"+ "\"})\n" + "RETURN file";
			Neo4jSession session = new Neo4jSession();
			session.runSessionQuery(fileCreateQuery);
			reader = new CSVReader(new FileReader(csvFilePath + sourceFileName), ',');
			String[] row = null;
			String[] rowHeaders = null;
			int i = 0;
			while ((row = reader.readNext()) != null) {
				int recordLength = row.length;
				String lineNumber = Integer.toString(i);
				if (i == 0) {
				
					// Create DefineField entries in database
					rowHeaders = row;
					System.out.println("Row Headers [" + recordLength + "] : " + Arrays.toString(rowHeaders));
				}

				if (i > 0) {
					
					// Create Record entries in database
					System.out.println("Row-" + lineNumber + " [" + recordLength + "] : " + Arrays.toString(row));

					// iterating over an array
					for (int j = 0; j < recordLength; j++) {
						
						String createQuery = null;
						if (j == 0) {
							// Create a new record & create fieldValue entry
							createQuery = "MATCH (file:File { fileName:\"" + mappingFileName+".csv"+"\"})\n" +
									"MATCH (mappingFile:mappingFiles {mappingName:\""+mappingFileName+"\"})\n" +
									"MATCH (mappingField: mappingFields{sourceField: \""+rowHeaders[j]+"\"})\n" +
									"WHERE  EXISTS (mappingField.functions)  AND mappingField.mappingFileID=mappingFile._id\n"+
									"MATCH (defineField: defineFields{_id: mappingField.defineFieldID})\n" +
									"OPTIONAL MATCH (defineSection: defineSections{_id: mappingField.defineSectionID})\n" +
									"MERGE (record:Record { number:\""+mappingFileName.replaceAll(" ","-")+"-"+lineNumber+"\"})-[:IN_FILE]->(file)\n" +
									"MERGE (field:Field { value:\"" + row[j] + "\"})\n" +
									"MERGE (field)<-[:IN_RECORD]-(record)\n" +
									"MERGE (field)<-[:DEFINITION]-(defineField)";
							session.runSessionQuery(createQuery);
							System.out.println("Created record : " + lineNumber);
						}

						if (j != 0) {
							// Match existing record & create fieldValue entry
							if (rowHeaders[j].matches("(?i)CPT code|ICD code|Request")) {

								String rowValue = row[j];
								String[] arrOfStr = rowValue.split("[,&]+");

								for (String a : arrOfStr) {
									System.out.println(a);
									a = a.replaceAll("\\s", "");
									createQuery = "MATCH (file:File { fileName:\"" +mappingFileName+".csv"+ "\"})\n" +
											"MATCH (mappingFile:mappingFiles {mappingName:\"" + mappingFileName + "\"})\n" +
											"MATCH (mappingField: mappingFields{sourceField: \"" + rowHeaders[j] + "\"})\n" +
											"WHERE  EXISTS (mappingField.functions)  AND mappingField.mappingFileID=mappingFile._id\n" +
											"MATCH (defineField: defineFields{_id: mappingField.defineFieldID})\n" +
											"MATCH (record:Record {number:\""+mappingFileName.replaceAll(" ","-")+"-"+lineNumber+"\"})\n" +
											"OPTIONAL MATCH (defineSection: defineSections{_id: mappingField.defineSectionID})\n" +
											"CREATE (field:Field { value:\"" + a + "\"})\n" +
											"MERGE (field)<-[:IN_RECORD]-(record)\n" +
											"MERGE (field)<-[:DEFINITION]-(defineField)";
									System.out.println("----Inserted CPT/ICD Data----");
									session.runSessionQuery(createQuery);
								}
							} else {
								System.out.println("----Inserting Records Data----");

								createQuery = "MATCH (file:File { fileName:\"" +mappingFileName+".csv"+ "\"})\n" +
										"MATCH (mappingFile:mappingFiles {mappingName:\""+mappingFileName+"\"})\n" +
										"MATCH (mappingField: mappingFields{sourceField: \""+rowHeaders[j]+"\"})\n" +
										"WHERE  EXISTS (mappingField.functions)  AND mappingField.mappingFileID=mappingFile._id\n"+
										"MATCH (defineField: defineFields{_id: mappingField.defineFieldID})\n" +
										"MATCH (record:Record { number:\""+mappingFileName.replaceAll(" ","-")+"-"+lineNumber+"\" })\n" +
										"OPTIONAL MATCH (defineSection: defineSections{_id: mappingField.defineSectionID})\n" +
										"CREATE (field:Field { value:\"" + row[j] + "\"})\n" +
										"MERGE (field)<-[:IN_RECORD]-(record)\n" +
										"MERGE (field)<-[:DEFINITION]-(defineField)";
								System.out.println("----Inserted Data----");
								session.runSessionQuery(createQuery);
							}
						}
					}
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}