package il.co.topq.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Configuration {

	INSTANCE;

	public enum ConfigProps {
		REPORT_DESTINATION_FOLDER("report_destination_folder");

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
		configProperties.put(ConfigProps.REPORT_DESTINATION_FOLDER.getValue(), "reports");
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
