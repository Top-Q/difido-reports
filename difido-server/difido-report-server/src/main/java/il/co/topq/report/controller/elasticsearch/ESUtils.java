package il.co.topq.report.controller.elasticsearch;

import il.co.topq.report.Common;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.index.query.QueryBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ESUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	private ESUtils(){
		//Utils
	}
	
	public static void update(String index, String type, String id, Object object) throws ElasticsearchException,
			JsonProcessingException {
		Common.elasticsearchClient.prepareUpdate().setIndex(index).setType(type).setId(id)
				.setDoc(mapper.writeValueAsString(object).getBytes()).execute().actionGet();
	}

	public static IndexResponse add(String index, String type, String id, Object object) throws JsonProcessingException {
		IndexRequest request = Requests.indexRequest(Common.ELASTIC_INDEX).id(id).type(type)
				.source(mapper.writeValueAsString(object));
		return Common.elasticsearchClient.index(request).actionGet();
	}

	/**
	 * Checks if the test already exists in the Elasticsearch. This can happen
	 * if the client sends the test details after the test ends.
	 * 
	 * @param uid
	 *            The test unique id.
	 * @return true if and only if a test with the specified UID already exists
	 *         in the Elasticsearch
	 */
	public static boolean isExist(String index, String type, String id) {
		SearchResponse response = Common.elasticsearchClient.prepareSearch(index).setTypes(type).setSearchType(SearchType.DEFAULT)
				.setQuery(QueryBuilders.termQuery("uid", id)).execute().actionGet();
		return response.getHits().getTotalHits() > 0;
	}
}
