package il.co.topq.report.business.elastic.client;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public class ESClient implements Closeable {

	private final ESRest rest;

	public ESClient(String host, int port) {
		rest = new ESRest(RestClient.builder(new HttpHost(host, port, "http")).build());
	}

	@Override
	public void close() throws IOException {
		if (rest != null) {
			rest.close();

		}
	}

	public Index index(String name) {
		return new Index(rest, name);
	}

}
