package com.thoughti.neo4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.neo4j.driver.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class Neo4jFetchFileInstanceData {
    public static void main(String[] args) throws Exception {
        JSONObject fileJson = null;
        ObjectMapper mapper = new ObjectMapper();
        Properties prop = new Properties();
        InputStream input = null;
        input = new FileInputStream("config.properties");
        prop.load(input);

        Driver driver = GraphDatabase.driver(prop.getProperty("neo4jport"),
                AuthTokens.basic(prop.getProperty("neo4jusername"), prop.getProperty("neo4jpassword")));
        org.neo4j.driver.Session session = driver.session();

        try {
            String fileLabelType = "Encounter";
            String fetchfileinstance = "MATCH (file:fileInstance)\n"
                    + "WHERE file.fileTypeLabel = \"" + fileLabelType + "\"\n"
                    + "WITH COLLECT({fileType:file.fileTypeLabel,fileinstance_id:file._id,client_id:file.clientID,patient_account_number:file.`Patient Account Number`,dos:file.`DOS Start`,prescription_date:file.`Prescription Date`}) AS files\n"
                    + "RETURN files";
            Result result = session.run(fetchfileinstance);
            Value fileJSONData = result.next().values().get(0);
            List fileInstanceData = fileJSONData.asList();
            JSONObject jsonObject = null;
            ElasticSearchPost elasticPost = new ElasticSearchPost();
            for (int i = 0; i < fileInstanceData.size(); i++) {
                System.out.println("File Data====>" + fileInstanceData.get(i));
                Gson gson = new Gson();
                String file = gson.toJson(fileInstanceData.get(i));
                fileJson = new JSONObject(file);
                String fileInstanceId = (String) fileJson.get("fileinstance_id");
                String fetchsectioninstance = "MATCH (section:sectionInstance)-[:OF_FILE]->(file:fileInstance{_id:\"" + fileInstanceId + "\"})\n"
                        + "WITH  COLLECT({sectioninstance_id:section._id,fields:section.fields,sectionLabel:section.sectionLabel}) AS sections\n"
                        + "RETURN sections";
                Result sectionresult = session.run(fetchsectioninstance);
                Value sectionJSONData = sectionresult.next().values().get(0);
                List sectionList = sectionJSONData.asList();

                String jsonData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sectionList);
                List list = mapper.readValue(jsonData, List.class);
                String jsonListKey = "";

                for (int j = 0; j < list.size(); j++) {
                    System.out.println("section Data====>" + list.get(j));
                    List<String> jsonList = null;
                    ArrayList search = new ArrayList();
                    int searchCount = 0;
                    Map listMap = (Map) list.get(j);
                    jsonListKey = listMap.get("sectionLabel").toString();
                    String fields = listMap.get("fields").toString();
                    String fields2 = fields.replaceAll("'defineFieldID'", "\"defineFieldID\"").replaceAll("'fieldLabel'", "\"fieldLabel\"").replaceAll("'value'", "\"value\"").replaceAll(": '", ": \"").replaceAll("'}", "\"}").replaceAll("',", "\",").replaceAll("ObjectId\\('", "\"").replaceAll("\\'\\)", "\"");
                    System.out.println(" Data====>" + fields2);
                    List<Map> listField = null;
                    try {
                        listField = mapper.readValue(fields2, List.class);
                    } catch (Exception e) {
                        break;
                    }
                    for (Map fieldMap : listField) {
                        String a = fieldMap.get("fieldLabel").toString();
                        String b = fieldMap.get("value").toString();

                        if (search.contains(a)) {
                            jsonList.add(b);
                            fileJson.put(a, jsonList);

                            System.out.println("JSON Data2====>" + fileJson.toString());
                        } else {
                            jsonList = new ArrayList<>();
                            search.add(a);
                            jsonList.add(b);
                            fileJson.put(a, jsonList);
                            System.out.println("JSON Data1====>" + fileJson.toString());

                        }
                    }

                }
                System.out.println("JSON Data====>" + fileJson.toString());
                elasticPost.postToElastic(fileJson.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            driver.close();
        }

    }
}
