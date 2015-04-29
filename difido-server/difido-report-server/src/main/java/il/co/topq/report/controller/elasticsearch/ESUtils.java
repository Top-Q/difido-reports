package il.co.topq.report.controller.elasticsearch;

import il.co.topq.report.Common;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ESUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final long UPDATE_TIME_OUT = 5000;

	private ESUtils() {
		// Utils
	}

	public static void update(String index, String type, String id, Object object) throws ElasticsearchException,
			JsonProcessingException {
		Common.elasticsearchClient.prepareUpdate().setIndex(index).setType(type).setId(id)
				.setDoc(mapper.writeValueAsString(object).getBytes()).execute().actionGet();
	}

	public static IndexResponse add(String index, String type, String id, Object object) throws JsonProcessingException {
		IndexRequest request = Requests.indexRequest(Common.ELASTIC_INDEX).id(id).type(type)
				.source(mapper.writeValueAsString(object));
		if (!StringUtils.isEmpty(id)) {
			request.id(id);
		}
		return Common.elasticsearchClient.index(request).actionGet();
	}

	public static IndexResponse add(String index, String type, Object object) throws JsonProcessingException {
		return add(index, type, null,object);
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
		SearchResponse response = Common.elasticsearchClient.prepareSearch(index).setTypes(type)
				.setSearchType(SearchType.DEFAULT).setQuery(QueryBuilders.termQuery("uid", id)).execute().actionGet();
		return response.getHits().getTotalHits() > 0;
	}

	public static IndexResponse safeAdd(String index, String type, Object object) throws Exception {
		return safeAdd(index, type, null, object);
	}

	/**
	 * Add document and wait for it to be indexed.
	 * 
	 * @param index
	 * @param type
	 * @param id
	 * @param object
	 * @throws Exception
	 *             if document was not index and timeout was reached <br>
	 *             If failed to add document
	 */
	public static IndexResponse safeAdd(String index, String type, String id, Object object) throws Exception {
		IndexResponse response = add(index, type, id, object);
		if (!response.isCreated()) {
			throw new Exception("Failed adding document from type '" + type + "' and id '" + id + "'");
		}
		id = response.getId();
		long startTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - startTime) < UPDATE_TIME_OUT) {
			if (isExist(index, type, id)) {
				return response;
			}
			Thread.sleep(100);
		}
		throw new Exception("Document with type '" + type + "' and id '" + id + "' was not indexed properly");
	}

	public static <T> T get(String index, String type, String id, Class<T> clazz) throws Exception {

		GetResponse response = Common.elasticsearchClient.prepareGet(index, type, id).execute().actionGet();
		if (!response.isExists()) {
			throw new Exception("Document with type '" + type + "' and id '" + id + "' is not exist");
		}
		return mapper.readValue(response.getSourceAsString(), clazz);
	}
}
