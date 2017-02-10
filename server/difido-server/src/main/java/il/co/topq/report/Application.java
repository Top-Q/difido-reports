package il.co.topq.report;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import org.apache.commons.io.FileUtils;
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
import il.co.topq.report.business.elastic.client.ESClient;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application extends SpringBootServletInitializer implements AsyncConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private static final String INDEX_SETTINGS_FILE = "mapping.json";

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
		try (ESClient client = new ESClient(Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST),
				Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT))) {
			if (client.index(Common.ELASTIC_INDEX).isExists()) {
				return;
			}

			// We are reading the mapping from external file and not using the
			// Java API since it seems that it is not possible to do a dynamic
			// mapping using the API
			final File settingsFile = new File(Common.CONFIUGRATION_FOLDER_NAME, INDEX_SETTINGS_FILE);
			if (!settingsFile.exists()) {
				logger.error("Failed to find elastic mapping file in " + settingsFile.getAbsolutePath()
						+ ". Will not be able to configure Elastic");
				return;
			}

			String settings = null;
			try {
				settings = FileUtils.readFileToString(settingsFile);
			} catch (IOException e) {
				logger.error("Failed to read mapping file. No index mapping will be set to the Elasticsearch", e);
				return;
			}

			client.index(Common.ELASTIC_INDEX).create(settings);

		} catch (IOException e) {
			logger.error("Failed to connect to Elasticsearc or to create index");
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

	}

	@Deprecated
	@SuppressWarnings("resource")
	public static void startElastic() throws UnknownHostException {
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ELASTIC_ENABLED)) {
			return;
		}

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