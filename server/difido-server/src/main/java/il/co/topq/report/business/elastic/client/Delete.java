package il.co.topq.report.business.elastic.client;

import java.io.IOException;
import java.util.Map;

public class Delete {

	private final ESRest client;
	
	private final String indexName;
	
	private final String documentName;

	public Delete(ESRest client, String indexName, String documentName) {
		this.client = client;
		this.indexName = indexName;
		this.documentName = documentName;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> single(String id) throws IOException {
		return client.delete(String.format("/%s/%s/%s", indexName, documentName, id), Map.class, true);
	}

}
