package il.co.topq.report.business.elastic;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.max.Max;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.report.Common;

public class ESUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final long UPDATE_TIME_OUT = 3000;

	private ESUtils() {
		// Utils
	}

	public static void update(String index, String type, String id, Object object)
			throws ElasticsearchException, JsonProcessingException {
		//@formatter:off
		Common.elasticsearchClient.
			prepareUpdate().
			setIndex(index).
			setType(type).
			setId(id).
			setDoc(mapper.
					writeValueAsString(object).
					getBytes()).
			execute().
			actionGet();
		//@formatter:on
	}

	/**
	 * 
	 * @param index
	 * @param type
	 * @param ids
	 * @param objects
	 * @return
	 * @throws JsonProcessingException 
	 */
	public static BulkResponse addBulk(String index, String type, String[] ids, List<?> objects) throws JsonProcessingException {
		if (ids.length != objects.size()){
			throw new IllegalArgumentException("Number of ids have to be equals to number of objects");
		}
		BulkRequestBuilder bulkRequest = Common.elasticsearchClient.prepareBulk();
		for (int i = 0 ; i < ids.length ; i++){
			bulkRequest.add(Common.elasticsearchClient.prepareIndex(index,type, ids[i])
					.setSource(mapper.writeValueAsBytes(objects.get(i)))
					.setId(ids[i])
					);
		}
		return bulkRequest.execute().actionGet();
	}
	
	public static IndexResponse add(String index, String type, String id, Object object)
			throws JsonProcessingException {
		//@formatter:off
		return Common.elasticsearchClient.prepareIndex(index, type)
		.setSource(mapper.writeValueAsBytes(object))
		.setId(id)
		.execute()
		.actionGet();
		//@formatter:on
	}

	public static IndexResponse add(String index, String type, Object object) throws JsonProcessingException {
		return add(index, type, null, object);
	}

	public static DeleteResponse delete(String index, String type, String id) {
		//@formatter:off
		return Common.elasticsearchClient
				.prepareDelete(index, type, id)
				.execute()
				.actionGet();
		//@formatter:on
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
		//@formatter:off
		return Common.elasticsearchClient.
				prepareGet(index, type, id).
				execute().
				actionGet().
				isExists();
		//@formatter:on
	}

	public static boolean isExistType(String index, String type) {
		//@formatter:off
		return Common.elasticsearchClient.
				prepareSearch(index).
				setTypes(type).
				setQuery(QueryBuilders.
						matchAllQuery()).
				execute().
				actionGet().
				getHits().
				getTotalHits() > 0;
		//@formatter:on

	}

	public static IndexResponse safeAdd(String index, String type, Object object) throws Exception {
		return safeAdd(index, type, null, object);
	}

	/**
	 * Add document and wait for it to be indexed. <br>
	 * NOTE: This is not always works as expected.
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

	/**
	 * Get the maximum value of specific numeric field.<br>
	 * e.g. Finding the biggest element id exits.
	 * 
	 * @param index
	 * @param type
	 * @param field
	 * @return max field value
	 */
	public static double maxFieldValue(String index, String type, String field) {
		return max(index, type, field, QueryBuilders.matchAllQuery());
	}

	/**
	 * Get the maximum value of specific numeric field after filtering with
	 * query.
	 * 
	 * @param index
	 * @param type
	 * @param field
	 * @param queryString
	 * @return max field value
	 */
	public static double maxFieldValueByQuery(String index, String type, String field, String queryString) {
		return max(index, type, field, queryStringQuery(queryString));
	}

	private static double max(String index, String type, String field, QueryBuilder query) {
		//@formatter:off
		final SearchResponse response = Common.elasticsearchClient.
				prepareSearch(index).
				setTypes(type).
				setQuery(query).
				addAggregation(AggregationBuilders.
						max("agg").
						field(field))
				.execute().
				actionGet();
		//@formatter:off
		double maxValue = 0;
		for (Aggregation maxAggs : response.getAggregations()) {
			Max max = (Max) maxAggs;
			maxValue = max.getValue();
		}
		return maxValue;
		
	}
	
	/**
	 * 
	 * 
	 * Sends a DSL query string and return list of all the objects that match the query. 
	 * More information on the DSL can be found 
	 * in the<br> <a href = "https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax">Elastic documentations</a> 
	 * 
	 * @param index
	 * @param type
	 * @param clazz
	 * @param queryString DSL String
	 * @return List of objects from the specified class
	 * @throws Exception
	 */
	public static <T> List<T> getAllByQuery(String index, String type, Class<T> clazz, String queryString) throws Exception {
		return query(index, clazz, queryStringQuery(queryString));
	}
	
	/**
	 * Get all the documents using the specified class that matches the specified filter
	 * 
	 * @param index
	 * @param type
	 * @param clazz
	 * @param filterTermKey
	 * @param filterTermValue
	 * @return list of objects from the specified class
	 * @throws Exception
	 */
	public static <T> List<T> getAllByTerm(String index, String type, Class<T> clazz, String filterTermKey, String filterTermValue) throws Exception {
		return query(index, clazz, termQuery(filterTermKey, filterTermValue));
	}

	private static <T> List<T> query(String index, Class<T> clazz, QueryBuilder qb)
			throws IOException, JsonParseException, JsonMappingException {
		//@formatter:off
		SearchResponse scrollResp =  Common.elasticsearchClient.prepareSearch(index)
		        .setScroll(new TimeValue(60000))
		        .setQuery(qb)
		        .setSize(100)
		        .execute().actionGet(); //100 hits per shard will be returned for each scroll
		//@formatter:on

		final List<T> results = new ArrayList<T>();

		// Scroll until no hits are returned
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				results.add(mapper.readValue(hit.getSourceAsString(), clazz));
			}
			//@formatter:off
		    scrollResp = Common.elasticsearchClient.prepareSearchScroll(
		    		scrollResp.getScrollId())
		    		.setScroll(new TimeValue(60000))
		    		.execute().actionGet();
		    //@formatter:on
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

		return results;
	}

	public static <T> T getById(String index, String type, String id, Class<T> clazz) throws Exception {
		//@formatter:off
		GetResponse response = Common.elasticsearchClient.
				prepareGet(index, type, id).
				execute().
				actionGet();
		//@formatter:on
		if (!response.isExists()) {
			throw new Exception("Document with type '" + type + "' and id '" + id + "' is not exist");
		}
		return mapper.readValue(response.getSourceAsString(), clazz);
	}
}
