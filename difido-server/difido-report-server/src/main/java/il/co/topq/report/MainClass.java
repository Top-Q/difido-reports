package il.co.topq.report;

import il.co.topq.report.Configuration.ConfigProps;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.apache.log4j.PropertyConfigurator;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 *
 */
public class MainClass {

	private static String baseUri;

	private static Node node;

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in com.example package
		final ResourceConfig rc = new ResourceConfig().packages("il.co.topq.report.controller.resource");
		rc.register(MultiPartFeature.class);
		rc.register(JacksonFeature.class);

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		baseUri = Configuration.INSTANCE.read(ConfigProps.BASE_URI);
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
		StaticHttpHandler staticHttpHandler = new StaticHttpHandler(
				Configuration.INSTANCE.read(ConfigProps.DOC_ROOT_FOLDER));
		staticHttpHandler.setFileCacheEnabled(false);
		server.getServerConfiguration().addHttpHandler(staticHttpHandler);
		return server;
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		configureLogger();
		final HttpServer server = startServer();
		startElastic();
		configureElastic();
		System.out.println(String.format("Report server started with WADL available at "
				+ "%sapplication.wadl\nHit enter to stop it...", baseUri));
		System.in.read();
		stopElastic();
		server.shutdownNow();
	}

	private static void configureLogger() {
		PropertyConfigurator.configure("config/log4j.properties");
	}

	public static void configureElastic() throws IOException {
		configureReportsIndex();
	}

	private static void configureReportsIndex() {
		HashMap<String, Object> not_analyzed_string = new HashMap<String, Object>();
		not_analyzed_string.put("type", "string");
		not_analyzed_string.put("index", "not_analyzed");

		HashMap<String, Object> analyzed_date = new HashMap<String, Object>();
		analyzed_date.put("type", "date");
		analyzed_date.put("format", Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.toPattern());

		HashMap<String, Object> not_analyzed_long = new HashMap<String, Object>();
		not_analyzed_long.put("type", "long");
		not_analyzed_long.put("index", "not_analyzed");

		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("name", not_analyzed_string);
		properties.put("uid", not_analyzed_string);
		properties.put("parent", not_analyzed_string);
		properties.put("status", not_analyzed_string);
		properties.put("url", not_analyzed_string);
		properties.put("description", not_analyzed_string);
		properties.put("execution", not_analyzed_string);
		properties.put("executionId", not_analyzed_long);
		properties.put("machine", not_analyzed_string);
		properties.put("timestamp", analyzed_date);
		properties.put("executionTimestamp", analyzed_date);
		properties.put("duration", not_analyzed_long);
		HashMap<String, Object> options = new HashMap<String, Object>();
		options.put("properties", properties);

		// client.admin().indices().prepareDelete("_all").get();
		IndicesExistsResponse res = Common.elasticsearchClient.admin().indices().prepareExists(Common.ELASTIC_INDEX)
				.execute().actionGet();
		if (!res.isExists()) {
			CreateIndexRequest request = Requests.createIndexRequest(Common.ELASTIC_INDEX).mapping("test", options);
			Common.elasticsearchClient.admin().indices().create(request).actionGet();

		}
	}

	public static void stopElastic() {
		node.close();
		Common.elasticsearchClient.close();
	}

	public static void startElastic() {
		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
		settings.put("node.name", "reportserver");
		settings.put("path.data", Configuration.INSTANCE.read(ConfigProps.PATH_DATA));
		settings.put("http.enabled", true);
		node = NodeBuilder.nodeBuilder().settings(settings).clusterName("reportserver").data(true).local(true).node();
		Common.elasticsearchClient = node.client();

	}

}
