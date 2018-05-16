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
		COMPRESS_FILES_ABOVE("compress.files.above","5000"),
		DONT_COMPRESS_EXTENTIONS("dont.compress.extensions","7z;zip;rar;gzip;gz;jpg;jpeg;png;gif;avi;xvid;mp4;mp3;tif;tiff;pdf;wmf;svg;exe;jar");
		
		
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
