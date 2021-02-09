package com.thoughti.neo4j;

import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.internal.value.ListValue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Neo4jFetch {
	public static void neo4jGetSession(String query) {
		
		try {
			Properties prop = new Properties();
			InputStream input = null;
			input = new FileInputStream("config.properties");
			prop.load(input);

			Driver driver = GraphDatabase.driver(prop.getProperty("neo4jport"),
					AuthTokens.basic(prop.getProperty("neo4jusername"), prop.getProperty("neo4jpassword")));
			try (org.neo4j.driver.Session session = driver.session()) 
			{
				Result result = session.run(query);
				System.out.println("neo4jFetchClass result==>" + result);
				Value neo4jJSONData = result.next().values().get(0);
				
				Iterable<Value> values = neo4jJSONData.values();
				Iterator<Value> valuesIterator = values.iterator();
				JSONObject jsonObject = null;
				List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
				ElasticSearchPost elasticPost = new ElasticSearchPost();
               
				while (valuesIterator.hasNext())
				{
					Value rowValue = valuesIterator.next();
					ListValue fields = (ListValue) rowValue.get("fields");
					int noOfFields = fields.size();
					jsonObject = new JSONObject();
					HashMap<String, Integer> fieldLabelMap = new HashMap<>();

					for (int i = 0; i < noOfFields; i++) {
						Value fieldValue = fields.get(i);
						String label = fieldValue.get("label", "");
						String value = fieldValue.get("value", "");
						String type = fieldValue.get("type", "");
						if (!value.trim().equalsIgnoreCase("")) {
							Integer counter = new Integer(1);
							if (fieldLabelMap.containsKey(label)) {
								counter = fieldLabelMap.get(label);
								counter = ++counter;
								fieldLabelMap.put(label, counter);

							} else {
								fieldLabelMap.put(label, counter);
							}
							label = label + "_" + counter;
							switch (type) {
							case "Number":
								jsonObject.put(label, Double.parseDouble(value));
							case "Alpha":
							default:
								jsonObject.put(label, value);
							}

							System.out.println("--------- label --> " + label + "       --- value --> " + value);
						} else {
							System.out.println("--------- label --> " + label + "        --- value empty");
						}

					}
					 jsonObjectList.add(jsonObject);

					 elasticPost.postToElastic(jsonObject.toString());
				}

				System.out.println("--------- jsonArray --> " + jsonObjectList);

			}finally {
				driver.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		
		String mappingFileName = System.getProperty("mappingFileName");

		String query = "MATCH (record:Record)-[:IN_RECORD]->(field:Field)\n"
				+ "MATCH (record:Record)-[:IN_FILE]->(file:File{ fileName:\"" + mappingFileName+".csv"+"\"})\n"
				+ "MATCH (defineField:defineFields)-[:DEFINITION]->(field)\n"
				+ "WITH record, collect({label:defineField.fieldLabel,value:field.value,type:defineField.fieldFormat}) AS fields\n"
				+ "WITH collect({number:record.number,fields:fields}) AS records\n" + "RETURN records";
		neo4jGetSession(query);

	}
}
