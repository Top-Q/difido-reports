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

		//@formatter:off
		BASE_URI("base.uri", "http://0.0.0.0:8080/api/"), 
		DOC_ROOT_FOLDER("doc.root.folder", "docRoot"), 
		PATH_DATA("path.data", "data/index"),
		MAX_EXECUTION_IDLE_TIME_IN_SEC("max.execution.idle.time.in.seconds", "600"),
		ENABLE_ELASTIC_SEARCH("enable.elastic.search", "true"), 
		ENABLE_HTML_REPORTS("enable.html.reports", "true"),
		ENABLE_MAIL("enable.mail","false"),				
		MAIL_USER_NAME("mail.user.name",""),
		MAIL_PASSWORD("mail.password",""),
		MAIL_SSL("mail.ssl","false"),
		MAIL_SMTP_HOST("mail.smtp.host",""),
		MAIL_SMTP_PORT("mail.smtp.port",""),
		MAIL_SUBJECT("mail.subject",""),
		MAIL_FROM_ADDRESS("mail.from.address",""),
		MAIL_TO_ADDRESS("mail.to.address",""),
		MAIL_CC_ADDRESS("mail.cc.address","");
		//@formatter:off
		
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

	private final static String CONFIG_PROP_NAME = "config/difido_config.properties";

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
		log.info("No configuration file found - Creating one with default parameters in "
				+ new File(CONFIG_PROP_NAME).getAbsolutePath());
		addPropWithDefaultValue(ConfigProps.BASE_URI);
		addPropWithDefaultValue(ConfigProps.DOC_ROOT_FOLDER);
		addPropWithDefaultValue(ConfigProps.PATH_DATA);
		addPropWithDefaultValue(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		addPropWithDefaultValue(ConfigProps.ENABLE_ELASTIC_SEARCH);
		addPropWithDefaultValue(ConfigProps.ENABLE_HTML_REPORTS);
		addPropWithDefaultValue(ConfigProps.ENABLE_MAIL);
		addPropWithDefaultValue(ConfigProps.MAIL_USER_NAME);
		addPropWithDefaultValue(ConfigProps.MAIL_PASSWORD);
		addPropWithDefaultValue(ConfigProps.MAIL_SMTP_HOST);
		addPropWithDefaultValue(ConfigProps.MAIL_SMTP_PORT);
		addPropWithDefaultValue(ConfigProps.MAIL_SSL);
		addPropWithDefaultValue(ConfigProps.MAIL_FROM_ADDRESS);
		addPropWithDefaultValue(ConfigProps.MAIL_TO_ADDRESS);
		addPropWithDefaultValue(ConfigProps.MAIL_CC_ADDRESS);
		try (FileOutputStream out = new FileOutputStream(new File(CONFIG_PROP_NAME))) {
			configProperties.store(out, "Default difido server properties");
		} catch (Exception e) {
			log.warn("Failed writing default configuration file", e);
		}
	}

	private void addPropWithDefaultValue(ConfigProps configProp) {
		configProperties.put(configProp.getPropName(), configProp.getDefaultValue());
	}

	public boolean readBoolean(ConfigProps prop) {
		return !"false".equals(readString(prop));
	}

	public int readInt(ConfigProps prop) {
		final String value = readString(prop);
		if (value != null && !value.isEmpty()) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	public String readString(ConfigProps prop) {
		String value = configProperties.getProperty(prop.getPropName());
		if (null == value) {
			return prop.getDefaultValue();
		}
		return value;
	}
}
