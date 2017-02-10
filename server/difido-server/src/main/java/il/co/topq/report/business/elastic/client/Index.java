package il.co.topq.report.business.elastic.client;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.RestClient;

public class Index extends AbstractSender {

	private final String indexName;

	public Index(RestClient client, String name) {
		super(client);
		this.indexName = name;
	}

	public boolean isExists() throws IOException {
		return head("/" + indexName, true).getStatusLine().getStatusCode() == 200;
	}

	public Index create(String settings) throws IOException {
		put("/" + indexName, settings, Map.class, true);
		return this;
	}

	public Document document(String documentName) {
		return new Document(client, indexName, documentName);
	}
	
}
