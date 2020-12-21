package com.thoughti.neo4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.JSONObject;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jSession {
	JSONObject jsonObject = new JSONObject();
	private static org.neo4j.driver.Session session;

	Properties prop = new Properties();
	InputStream input = null;

	public Neo4jSession() {
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			Driver driver = GraphDatabase.driver(prop.getProperty("neo4jport"),	AuthTokens.basic(prop.getProperty("neo4jusername"), prop.getProperty("neo4jpassword")));
			session = driver.session();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void runSessionQuery(String query) {
		try {
			session.run(query);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}