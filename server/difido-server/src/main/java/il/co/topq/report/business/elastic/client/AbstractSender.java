package il.co.topq.report.business.elastic.client;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractSender {

	protected static final ObjectMapper mapper = new ObjectMapper();

	protected final RestClient client;

	public AbstractSender(RestClient client) {
		this.client = client;
	}

	protected <T> T post(String resource, String body, Class<T> responseClass, boolean assertSuccess)
			throws IOException {
		final Response response = client.performRequest("POST", resource, Collections.singletonMap("pretty", "true"),
				new NStringEntity(body, ContentType.APPLICATION_JSON));
		if (assertSuccess) {
			assertSuccess(response);
		}
		return mapper.readValue(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), responseClass);
	}

	protected Response head(String resource, boolean assertSuccess) throws IOException {
		return client.performRequest("HEAD", resource, Collections.singletonMap("pretty", "true"));
	}

	protected <T> T put(String resource, String body ,Class<T> responseClass, boolean assertSuccess) throws IOException {
		final HttpEntity entity = new NStringEntity(body, ContentType.APPLICATION_JSON);
		final Response response = client.performRequest("PUT", resource, Collections.<String, String>emptyMap(),
				entity);
		if (assertSuccess) {
			assertSuccess(response);
		}
		return mapper.readValue(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), responseClass);
	}

	protected <T> T delete(String resource,Class<T> responseClass, boolean assertSuccess) throws IOException {
		final Response response = client.performRequest("DELETE", resource, Collections.<String, String>emptyMap());
		if (assertSuccess) {
			assertSuccess(response);
		}
		return mapper.readValue(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), responseClass);
	}
	
	private void assertSuccess(Response response) throws IOException {
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException("Return status is " + response.getStatusLine().getStatusCode());
		}
	}


}
