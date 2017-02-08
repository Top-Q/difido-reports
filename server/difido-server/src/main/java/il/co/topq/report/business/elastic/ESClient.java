package il.co.topq.report.business.elastic;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ESClient implements Closeable {

	private final static Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();

	private static final ObjectMapper mapper = new ObjectMapper();
	
	private final RestClient rest;

	public ESClient(String host, int port) {
		rest = RestClient.builder(new HttpHost(host, port, "http")).build();
	}

	public boolean isIndexExists(String indexName) throws IOException {
		final Response response = rest.performRequest("HEAD", "/" + indexName,
				Collections.singletonMap("pretty", "true"));
		if (response.getStatusLine().getStatusCode() == 200) {
			return true;
		}
		return false;

	}

	public void createIndex(String indexName, String indexSettings) throws IOException {
		final HttpEntity entity = new NStringEntity(indexSettings, ContentType.APPLICATION_JSON);
		rest.performRequest("PUT", "/" + indexName, EMPTY_MAP, entity);
	}

	public <T> List<T> getAllByTerm(String index, String type, Class<T> clazz, String filterTermKey,
			String filterTermValue) throws Exception {

		String requestBody = String.format("{\"query\": {\"term\" : { \"%s\" : \"%s\" }  } }", filterTermKey, filterTermValue);
		final Response response = rest.performRequest("POST", "/" + index + "/" + type +"/_search",  Collections.singletonMap("pretty", "true"),  new NStringEntity(requestBody, ContentType.APPLICATION_JSON));
		String responseBody = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		ElasticResponse elResponse = mapper.readValue(responseBody, ElasticResponse.class);
		for (Hit h : elResponse.getHits().getHits()){
			System.out.println(h.getSource().getName());
			
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		if (rest != null) {
			rest.close();

		}
	}
	public static void main(String[] args) throws Exception {
		ESClient c = new ESClient("localhost",9200);
		c.getAllByTerm("report", "test", null, "executionId", "4");
		c.close();
	}

}
