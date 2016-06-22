package il.co.topq.report;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import il.co.topq.report.Configuration.ConfigProps;

@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private static final String MAPPING_FILE = "mapping.json";

	private static Node node;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		startElastic();
		configureReportsIndex();
		SpringApplication.run(Application.class, args);
		// stopElastic();
	}

	private static void configureReportsIndex() {
		final IndicesExistsResponse res = Common.elasticsearchClient.admin().indices()
				.prepareExists(Common.ELASTIC_INDEX).execute().actionGet();
		if (res.isExists()) {
			// Index is already exists, so there is no need to configure the
			// mapping
			return;
		}
		// We are reading the mapping from external file and not using the
		// Java API since it seems that it is not possible to do a dynamic
		// mapping using the API
		final File mappingFile = new File(Common.CONFIUGRATION_FOLDER_NAME, MAPPING_FILE);
		if (!mappingFile.exists()) {
			logger.error("Failed to find elastic mapping file in " + mappingFile.getAbsolutePath()
					+ ". Will not be able to configure Elastic");
			return;
		}

		String mapping = null;
		try {
			mapping = FileUtils.readFileToString(mappingFile);
		} catch (IOException e) {
			logger.error("Failed to read mapping file. No index mapping will be set to the Elasticsearch", e);
			return;
		}
		final CreateIndexRequest request = Requests.createIndexRequest(Common.ELASTIC_INDEX).mapping("test", mapping);
		Common.elasticsearchClient.admin().indices().create(request).actionGet();

	}

	public static void stopElastic() {
		if (node != null) {
			node.close();
		}
		Common.elasticsearchClient.close();
	}

	public static void startElastic() throws UnknownHostException {
		Settings.Builder settings = Settings.settingsBuilder();
		settings.put("node.name", "reportserver");
		if (Configuration.INSTANCE.readBoolean(ConfigProps.EXTERNAL_ELASTIC)) {
			final String host = Configuration.INSTANCE.readString(ConfigProps.EXTERNAL_ELASTIC_HOST);
			final int port = Configuration.INSTANCE.readInt(ConfigProps.EXTERNAL_ELASTIC_PORT);
			Common.elasticsearchClient = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
		} else {
			settings.put("cluster.name", "reportserver");
			settings.put("path.data", Configuration.INSTANCE.readString(ConfigProps.PATH_DATA));
			settings.put("http.enabled", true);
			settings.put("path.home", ".");
			node = NodeBuilder.nodeBuilder().settings(settings).data(true).local(true).node();
			Common.elasticsearchClient = node.client();


		}

	}

}