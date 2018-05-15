package il.co.topq.difido.config;

public class RemoteDifidoConfig extends AbstractPropertiesConfigFile {

	public RemoteDifidoConfig() {
		super(RemoteDifidoOptions.values());
	}

	@Override
	protected String getFileName() {
		return "remoteDifido.properties";
	}

	public enum RemoteDifidoOptions implements ConfigOptions {
		// @formatter:off
		HOST("host", "localhost"),
		PORT("port", "8080"),
		ENABLED("enabled", "true"),
		DESCRIPTION("execution.description", ""),
		EXECUTION_PROPETIES("execution.properties", ""),
		USE_SHARED_EXECUTION("use.shared.execution", "false"),
		EXISTING_EXECUTION_ID("existing.execution.id", "-1"),
		FORCE_NEW_EXECUTION("force.new.execution", "false"),
		APPEND_TO_EXISTING_EXECUTION("append.to.existing.execution", "false"),
		COMPRESS_FILES_ABOVE("compress.files.above","5000");
		
		// @formatter:on

		private String property;

		private String defaultValue;

		private RemoteDifidoOptions(final String property, final String defaultValue) {
			this.property = property;
			this.defaultValue = defaultValue;
		}

		@Override
		public String getProperty() {
			return property;
		}

		@Override
		public String getDefaultValue() {
			return defaultValue;
		}

		@Override
		public ConfigOptions[] getOptions() {
			return RemoteDifidoOptions.values();
		}

	}

}
