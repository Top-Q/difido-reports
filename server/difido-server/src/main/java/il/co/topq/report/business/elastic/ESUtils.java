package il.co.topq.report.business.elastic;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.report.Common;

public class ESUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	private ESUtils() {
		// Utils
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
		SearchResponse scrollResp =  Common.elasticsearchJavaClient.prepareSearch(index)
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
		    scrollResp = Common.elasticsearchJavaClient.prepareSearchScroll(
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
	
	public static DeleteResponse delete(String index, String type, String id) {
		//@formatter:off
		return Common.elasticsearchJavaClient
				.prepareDelete(index, type, id)
				.execute()
				.actionGet();
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
	public static BulkResponse addBulk(String index, String type, String[] ids, List<?> objects)
			throws JsonProcessingException {
		if (ids.length != objects.size()) {
			throw new IllegalArgumentException("Number of ids have to be equals to number of objects");
		}
		BulkRequestBuilder bulkRequest = Common.elasticsearchJavaClient.prepareBulk();
		for (int i = 0; i < ids.length; i++) {
			bulkRequest.add(Common.elasticsearchJavaClient.prepareIndex(index, type, ids[i])
					.setSource(mapper.writeValueAsBytes(objects.get(i))).setId(ids[i]));
		}
		return bulkRequest.execute().actionGet();
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
	
	
}
