package il.co.topq.difido.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class AbstractPropertiesConfigFile {

	private static final Logger log = Logger.getLogger(AbstractPropertiesConfigFile.class.getName());

	private Properties properties;

	private ConfigOptions[] options;

	public AbstractPropertiesConfigFile(ConfigOptions[] options) {
		this.options = options;
		properties = new Properties();
		final File propertiesFile = new File(getFileName());
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

	protected abstract String getFileName();

	private void initDefaultProperties() {
		for (ConfigOptions option : options[0].getOptions()) {
			properties.setProperty(option.getProperty(), option.getDefaultValue());
		}
		final File propertiesFile = new File(getFileName());
		if (propertiesFile.exists()) {
			propertiesFile.delete();
		}
		try (final FileWriter writer = new FileWriter(propertiesFile)) {
			properties.store(writer, "Default Difido properties");
		} catch (IOException e) {
			log.warning("Failed to write Difido properties to file");
		}
	}

	public String getPropertyAsString(ConfigOptions option) {
		String value = System.getProperty(option.getProperty());
		if (value != null) {
			return value.trim();
		}
		value = properties.getProperty(option.getProperty());
		if (value == null || value.isEmpty()){
			return "";
		}
		return value.trim();
	}

	public boolean getPropertyAsBoolean(ConfigOptions option) {
		final String value = getPropertyAsString(option);
		return Boolean.parseBoolean(value);
	}

	public int getPropertyAsInt(ConfigOptions option) {
		final String value = getPropertyAsString(option);
		return Integer.parseInt(value);
	}

	public List<String> getPropertyAsList(ConfigOptions option) {
		final String values = getPropertyAsString(option);
		List<String> valueList = new ArrayList<String>();
		if (values == null || values.isEmpty()) {
			return valueList;
		}
		for (String singleValue : values.split(";")) {
			valueList.add(singleValue);
		}
		return valueList;
	}

	public Map<String, String> getPropertyAsMap(ConfigOptions option) {
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
