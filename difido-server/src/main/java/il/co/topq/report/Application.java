package il.co.topq.report;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
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
			return;
		}
		String mappingJson = null;
		try {
			// We are reading the mapping from external file and not using the
			// Java API since it seems that it is not possible to do a dynamic
			// mapping using the API
			mappingJson = IOUtils.toString(Application.class.getClassLoader().getResourceAsStream(MAPPING_FILE));
		} catch (IOException e) {
			logger.error("Failed to read mapping file. No index mapping will be set to the Elasticsearch", e);
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
		Settings.Builder settings = Settings.settingsBuilder();
		settings.put("node.name", "reportserver");
		settings.put("path.data", Configuration.INSTANCE.readString(ConfigProps.PATH_DATA));
		settings.put("http.enabled", true);
		settings.put("path.home", ".");
		node = NodeBuilder.nodeBuilder().settings(settings).clusterName("reportserver").data(true).local(true).node();
		Common.elasticsearchClient = node.client();

	}

}