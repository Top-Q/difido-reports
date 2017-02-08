package il.co.topq.report;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.Executor;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import il.co.topq.report.Configuration.ConfigProps;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application extends SpringBootServletInitializer implements AsyncConfigurer {

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
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ELASTIC_ENABLED)) {
			return;
		}
		try {
			Response response = Common.elasticsearchRestClient.performRequest("HEAD", "/report",
					Collections.singletonMap("pretty", "true"));
			logger.debug("Response code is: " + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
				// Index is already exists, so there is no need to configure the
				// mapping
				logger.debug("Report index exists");
				return;
			}
		} catch (IOException e1) {
			logger.error("Failed to connect to Elastic. ");
			return;
		}

		try {
			HttpEntity entity = new NStringEntity(
					"{\"settings\" : {\"index\" : {\"number_of_shards\" : 3,\"number_of_replicas\" : 1}}}",
					ContentType.APPLICATION_JSON);
			Common.elasticsearchRestClient.performRequest("PUT", "/report", Collections.<String, String> emptyMap(),
					entity);
		} catch (IOException e1) {
			logger.error("Failed to create new index");
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

		HttpEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON);
		try {
			Common.elasticsearchRestClient.performRequest("PUT", "/report", Collections.<String, String> emptyMap(),
					entity);
		} catch (IOException e) {
			logger.error("Failed to create mappings");
			return;
		}
	}

	public static void stopElastic() {
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ELASTIC_ENABLED)) {
			return;
		}

		if (node != null) {
			try {
				node.close();
			} catch (IOException e) {
				logger.warn("Failed to close Elastic");
			}
		}
		Common.elasticsearchJavaClient.close();

		try {
			Common.elasticsearchRestClient.close();
		} catch (IOException e) {
			logger.warn("Failed to close Elastic Rest client");
		}
	}

	@SuppressWarnings("resource")
	public static void startElastic() throws UnknownHostException {
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ELASTIC_ENABLED)) {
			return;
		}

		Common.elasticsearchRestClient = RestClient
				.builder(new HttpHost(Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST),
						Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT), "http"))
				.build();

		Settings settingsBuilder = Settings.builder().put("node.name", "reportserver").build();
		final String host = Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST);
		final int port = Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_TRANSPORT_TCP_PORT);
		Common.elasticsearchJavaClient = new PreBuiltTransportClient(settingsBuilder)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

	}

	/**
	 * Configuration of the async executor. This is used for writing to the file
	 * system and it is very important that there will be no more then one
	 * thread in the pool.
	 */
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// Do not change the number of threads here
		executor.setCorePoolSize(1);
		// Do not change the number of threads here
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(10000);
		executor.setThreadNamePrefix("AsyncActionQueue-");
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		// Unused
		return null;
	}

}