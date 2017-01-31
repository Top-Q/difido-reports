package il.co.topq.difido;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class RemoteDifidoProperties {

	private static final Logger log = Logger.getLogger(RemoteDifidoProperties.class.getName());

	public static final String FILE_NAME = "remoteDifido.properties";

	private Properties properties;

	enum RemoteDifidoOptions {
		// @formatter:off
		HOST("host", "localhost"),
		PORT("port", "8080"),
		ENABLED("enabled", "true"),
		DESCRIPTION("execution.description", ""),
		EXECUTION_PROPETIES("execution.properties", ""),
		USE_SHARED_EXECUTION("use.shared.execution", "false"),
		EXISTING_EXECUTION_ID("existing.execution.id", "-1"),
		FORCE_NEW_EXECUTION("force.new.execution", "false"),
		APPEND_TO_EXISTING_EXECUTION("append.to.existing.execution", "false");
		// @formatter:on

		private String property;

		private String defaultValue;

		private RemoteDifidoOptions(final String property, final String defaultValue) {
			this.property = property;
			this.defaultValue = defaultValue;
		}

		public String getProperty() {
			return property;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

	}

	public RemoteDifidoProperties() {
		properties = new Properties();
		final File propertiesFile = new File(FILE_NAME);
		if (!propertiesFile.exists()) {
			initDefaultProperties();
		}
		try (final FileReader reader = new FileReader(propertiesFile)) {
			properties.load(reader);
			if (properties.isEmpty()) {
				initDefaultProperties();
			}
		} catch (IOException e) {
			initDefaultProperties();
		}
	}

	private void initDefaultProperties() {
		for (RemoteDifidoOptions option:RemoteDifidoOptions.values()){
			properties.setProperty(option.getProperty(), option.getDefaultValue());
		}
		final File propertiesFile = new File(FILE_NAME);
		if (propertiesFile.exists()) {
			propertiesFile.delete();
		}
		try (final FileWriter writer = new FileWriter(propertiesFile)) {
			properties.store(writer, "Default Difido properties");
		} catch (IOException e) {
			log.warning("Failed to write Difido properties to file");
		}
	}

	public String getPropertyAsString(RemoteDifidoOptions option) {
		return properties.getProperty(option.getProperty());
	}

	public boolean getPropertyAsBoolean(RemoteDifidoOptions option) {
		final String value = getPropertyAsString(option);
		return Boolean.parseBoolean(value);
	}

	public int getPropertyAsInt(RemoteDifidoOptions option) {
		final String value = getPropertyAsString(option);
		return Integer.parseInt(value);
	}

	public Map<String, String> getPropertyAsMap(RemoteDifidoOptions option) {
		final String value = getPropertyAsString(option);
		Map<String, String> valueMap = new HashMap<String, String>();
		if (value == null || value.isEmpty()) {
			return valueMap;
		}
		String[] keyValuePairArr = value.split(";");
		for (String keyValuePair : keyValuePairArr) {
			try {
				valueMap.put(keyValuePair.split("=")[0], keyValuePair.split("=")[1]);
			} catch (Exception e) {

			}

		}
		return valueMap;
	}

}
