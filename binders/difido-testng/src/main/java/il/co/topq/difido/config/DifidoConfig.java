package il.co.topq.difido.config;

public class DifidoConfig extends AbstractPropertiesConfigFile {

	public DifidoConfig() {
		super(DifidoOptions.values());
	}

	public enum DifidoOptions implements ConfigOptions {
		// @formatter:off
		REPORTER_CLASSES("reporter.classes", "il.co.topq.difido.LocalDifidoReporter;il.co.topq.difido.RemoteDifidoReporter");
		// @formatter:on

		private String property;

		private String defaultValue;

		private DifidoOptions(final String property, final String defaultValue) {
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
			return DifidoOptions.values();
		}

	}

	@Override
	protected String getFileName() {
		return "difido.properties";
	}

}
