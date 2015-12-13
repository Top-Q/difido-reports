package il.co.topq.report;

import il.co.topq.report.Configuration.ConfigProps;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class.
 * 
 */
public class MainClass {

	private static final Logger log = LoggerFactory.getLogger(MainClass.class);

	private static final String MAPPING_FILE = "mapping.json";

	private static final String LOG_PROPERTIES_FILE = "config/log4j.properties";

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
		baseUri = Configuration.INSTANCE.readString(ConfigProps.BASE_URI);
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
		StaticHttpHandler staticHttpHandler = new StaticHttpHandler(
				Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER));
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
		PropertyConfigurator.configure(LOG_PROPERTIES_FILE);
	}

	public static void configureElastic() throws IOException {
		configureReportsIndex();
	}

	private static void configureReportsIndex() {
		final IndicesExistsResponse res = Common.elasticsearchClient.admin().indices()
				.prepareExists(Common.ELASTIC_INDEX).execute().actionGet();
		if (res.isExists()) {
			return;
		}
		String mappingJson = null;
		try {
			// We are reading the mapping from external file and not using the
			// Java API since it seems that it is not possible to do a dynamic
			// mapping using the API
			mappingJson = IOUtils.toString(MainClass.class.getClassLoader().getResourceAsStream(MAPPING_FILE));
		} catch (IOException e) {
			log.error("Failed to read mapping file. No index mapping will be set to the Elasticsearch", e);
			return;
		}
		final CreateIndexRequest request = Requests.createIndexRequest(Common.ELASTIC_INDEX).mapping("test",
				mappingJson);
		Common.elasticsearchClient.admin().indices().create(request).actionGet();

	}

	public static void stopElastic() {
		node.close();
		Common.elasticsearchClient.close();
	}

	public static void startElastic() {
		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
		settings.put("node.name", "reportserver");
		settings.put("path.data", Configuration.INSTANCE.readString(ConfigProps.PATH_DATA));
		settings.put("http.enabled", true);
		node = NodeBuilder.nodeBuilder().settings(settings).clusterName("reportserver").data(true).local(true).node();
		Common.elasticsearchClient = node.client();

	}

}
