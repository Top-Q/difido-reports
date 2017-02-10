package il.co.topq.report.business.elastic.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.report.business.elastic.client.response.QueryResponse;

public class QueryResultHandler {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final List<QueryResponse> queryResponseList;

	public QueryResultHandler(List<QueryResponse> response) {
		super();
		this.queryResponseList = response;
	}

	public List<Map<String, Object>> asMap() {
		List<Map<String, Object>> asMapResponse = new ArrayList<Map<String, Object>>();
//		@formatter:off
		queryResponseList.stream()
			.map(response -> response.getHits().getHits())
			.flatMap(List::stream)
			.map(hit -> hit.getDataSource())
			.forEach(asMapResponse::add);
//		@formatter:on
		return asMapResponse;
	}

	public <T> List<T> asClass(Class<T> clazz) {
		List<T> response = new ArrayList<T>();
		asMap().stream().map(hit -> mapper.convertValue(hit, clazz)).forEach(response::add);
		return response;

	}

}
