package il.co.topq.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Configuration {

	INSTANCE;

	public enum ConfigProps {
		
		BASE_URI("base_uri"),
		DOC_ROOT_FOLDER("doc_root_folder"),
		MAX_EXECUTION_IDLE_TIME_IN_SEC("max_execution_idle_time_in_seconds");
		
		private String value;

		private ConfigProps(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

	private Logger log = Logger.getLogger(Configuration.class.getSimpleName());

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
			log.log(Level.WARNING, "Failure in reading filw " + CONFIG_PROP_NAME
					+ ". Rolling back to default properties", e);
		}

	}

	private void useDefaultProperties() {
		log.info("Using default properties");
		configProperties.put(ConfigProps.BASE_URI.getValue(), "http://localhost:8080/api/");
		configProperties.put(ConfigProps.DOC_ROOT_FOLDER.getValue(), "docRoot");
		configProperties.put(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC.getValue(), "600");
		try (FileOutputStream out = new FileOutputStream(new File(CONFIG_PROP_NAME))) {
			configProperties.store(out, "Default difido server proerties");
		} catch (Exception e) {
			log.log(Level.WARNING, "Failed writing default configuration file", e);
		}
	}

	public String read(ConfigProps prop) {
		return configProperties.getProperty(prop.getValue());
	}
}
