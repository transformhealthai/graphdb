package com.thoughti.neo4j;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ElasticSearchPost {
	public boolean createIndex(String indexName) throws IOException {
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream("config.properties");
		prop.load(input);
		RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
				new HttpHost(prop.getProperty("elastichostname"), Integer.parseInt(prop.getProperty("elasticport1")), prop.getProperty("elasticscheme")),
				new HttpHost(prop.getProperty("elastichostname"), Integer.parseInt(prop.getProperty("elasticport2")), prop.getProperty("elasticscheme"))));

		CreateIndexRequest request = new CreateIndexRequest(indexName);
		try {
			CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	public void postToElastic(String jsonDoc) throws IOException {
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream("config.properties");
		prop.load(input);
		RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
				new HttpHost(prop.getProperty("elastichostname"), Integer.parseInt(prop.getProperty("elasticport1")), prop.getProperty("elasticscheme")),
				new HttpHost(prop.getProperty("elastichostname"), Integer.parseInt(prop.getProperty("elasticport2")), prop.getProperty("elasticscheme"))));
		IndexRequest indexRequest = new IndexRequest(prop.getProperty("elasticindex"));
		try {
			indexRequest.source(jsonDoc, XContentType.JSON);
			IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
