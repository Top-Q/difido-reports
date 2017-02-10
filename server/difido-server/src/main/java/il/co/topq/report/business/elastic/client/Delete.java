package il.co.topq.report.business.elastic.client;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.RestClient;

public class Delete extends AbstractSender {

	private final String indexName;
	private final String documentName;

	public Delete(RestClient client, String indexName, String documentName) {
		super(client);
		this.indexName = indexName;
		this.documentName = documentName;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> single(String id) throws IOException {
		return delete(String.format("/%s/%s/%s", indexName, documentName, id), Map.class, true);
	}

}
