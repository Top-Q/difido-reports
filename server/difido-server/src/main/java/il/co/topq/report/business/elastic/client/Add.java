package il.co.topq.report.business.elastic.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.RestClient;

public class Add extends AbstractSender {

	private String indexName;
	private String documentName;

	public Add(RestClient client, String indexName, String documentName) {
		super(client);
		this.indexName = indexName;
		this.documentName = documentName;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> bulk(String[] ids, List<?> objects) throws IOException{
		if (ids.length != objects.size()) {
			throw new IllegalArgumentException("Number of ids have to be equals to number of objects");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i < ids.length ; i++){
			sb.append(String.format("{ \"create\": {\"_id\":\"%s\"}\n", ids[i]));
			sb.append(mapper.writeValueAsString(objects.get(i))).append("\n");
		}
		return post(String.format("/%s/%s/_bulk", indexName,documentName),sb.toString(),Map.class,true);
	}

}
