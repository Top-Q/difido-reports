package il.co.topq.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Configuration {

	INSTANCE;

	public enum ConfigProps {

		BASE_URI("base_uri", "http://localhost:8080/api/"), DOC_ROOT_FOLDER("doc_root_folder", "docRoot"), PATH_DATA(
				"path_data", "data/index"), MAX_EXECUTION_IDLE_TIME_IN_SEC("max_execution_idle_time_in_seconds", "600"), ENABLE_ELASTIC_SEARCH(
				"enable_elastic_search", "true"), ENABLE_HTML_REPORTS("enable_html_reports", "true");

		private final String propName;

		private final String defaultValue;

		private ConfigProps(String value, String defaultValue) {
			this.propName = value;
			this.defaultValue = defaultValue;
		}

		public String getPropName() {
			return propName;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

	}

	private final Logger log = LoggerFactory.getLogger(Configuration.class);

	private final static String CONFIG_PROP_NAME = "difido_config.properties";

	private Properties configProperties = new Properties();

	private Configuration() {
		if (!new File(CONFIG_PROP_NAME).exists()) {
			useDefaultProperties();
			return;
		}
		readConfigurationFromFile();
		if (configProperties.isEmpty()) {
			useDefaultProperties();
		}

	}

	private void readConfigurationFromFile() {
		try (FileReader reader = new FileReader(new File(CONFIG_PROP_NAME))) {
			configProperties.load(reader);

		} catch (Exception e) {
			log.warn("Failure in reading filw " + CONFIG_PROP_NAME + ". Rolling back to default properties", e);
		}

	}

	private void useDefaultProperties() {
		log.info("Using default properties");
		configProperties.put(ConfigProps.BASE_URI.getPropName(), ConfigProps.BASE_URI.getDefaultValue());
		configProperties.put(ConfigProps.DOC_ROOT_FOLDER.getPropName(), ConfigProps.DOC_ROOT_FOLDER.getDefaultValue());
		configProperties.put(ConfigProps.PATH_DATA.getPropName(), ConfigProps.PATH_DATA.getDefaultValue());
		configProperties.put(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC.getPropName(),
				ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC.getDefaultValue());
		configProperties.put(ConfigProps.ENABLE_ELASTIC_SEARCH.getPropName(),
				ConfigProps.ENABLE_ELASTIC_SEARCH.getDefaultValue());
		configProperties.put(ConfigProps.ENABLE_HTML_REPORTS.getPropName(),
				ConfigProps.ENABLE_HTML_REPORTS.getDefaultValue());
		try (FileOutputStream out = new FileOutputStream(new File(CONFIG_PROP_NAME))) {
			configProperties.store(out, "Default difido server properties");
		} catch (Exception e) {
			log.warn("Failed writing default configuration file", e);
		}
	}

	public boolean readBoolean(ConfigProps prop) {
		return !"false".equals(read(prop));
	}

	public int readInt(ConfigProps prop) {
		final String value = read(prop);
		if (value != null && !value.isEmpty()) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	public String read(ConfigProps prop) {
		String value = configProperties.getProperty(prop.getPropName());
		if (null == value) {
			return prop.getDefaultValue();
		}
		return value;
	}
}
