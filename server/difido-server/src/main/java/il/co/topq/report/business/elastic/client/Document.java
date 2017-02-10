package il.co.topq.report.business.elastic.client;

import org.elasticsearch.client.RestClient;

public class Document {

	private final RestClient client;

	private final String indexName;

	private final String documentName;

	public Document(RestClient client, String indexName, String documentName) {
		super();
		this.client = client;
		this.indexName = indexName;
		this.documentName = documentName;
	}

	public Query query() {
		return new Query(client, indexName, documentName, 100, true);
	}

	public Add add() {
		return new Add(client, indexName, documentName);
	}

	public Delete delete() {
		return new Delete(client, indexName, documentName);
	}

}
