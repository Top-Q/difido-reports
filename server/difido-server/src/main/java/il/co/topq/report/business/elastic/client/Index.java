package il.co.topq.report.business.elastic.client;

import java.io.IOException;
import java.util.Map;

public class Index {

	private final ESRest client;

	private final String indexName;

	public Index(ESRest client, String name) {
		this.client = client;
		this.indexName = name;
	}

	public boolean isExists() throws IOException {
		return client.head("/" + indexName, true) == 200;
	}

	public Index create(String settings) throws IOException {
		client.put("/" + indexName, settings, Map.class, true);
		return this;
	}

	public Index delete() throws IOException {
		client.delete("/" + indexName, Map.class, true);
		return this;
	}

	public Document document(String documentName) {
		return new Document(client, indexName, documentName);
	}

}
