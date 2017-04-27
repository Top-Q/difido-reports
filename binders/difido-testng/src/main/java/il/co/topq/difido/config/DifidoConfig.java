package il.co.topq.difido.config;

import il.co.topq.difido.reporters.ConsoleReporter;
import il.co.topq.difido.reporters.LocalDifidoReporter;
import il.co.topq.difido.reporters.RemoteDifidoReporter;

public class DifidoConfig extends AbstractPropertiesConfigFile {

	public DifidoConfig() {
		super(DifidoOptions.values());
	}

	public enum DifidoOptions implements ConfigOptions {
		// @formatter:off
		REPORTER_CLASSES("reporter.classes", LocalDifidoReporter.class.getName() +";" + RemoteDifidoReporter.class.getName() +";" +ConsoleReporter.class.getName()),
		MIN_TIME_BETWEEN_WRITES("min.time.between.writes","100");
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
