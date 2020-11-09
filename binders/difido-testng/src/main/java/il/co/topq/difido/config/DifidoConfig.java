package il.co.topq.difido.config;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import il.co.topq.difido.reporters.ConsoleReporter;
import il.co.topq.difido.reporters.LocalDifidoReporter;
import il.co.topq.difido.reporters.RemoteDifidoReporter;

public class DifidoConfig extends AbstractPropertiesConfigFile {

	private static final Logger log = Logger.getLogger(DifidoConfig.class.getName());
	
	public DifidoConfig() {
		super(DifidoOptions.values());
		setLogLevel();
	}

	private void setLogLevel() {
		final String logLevelStr = getPropertyAsString(DifidoOptions.LOG_LEVEL);
		if (logLevelStr == null || logLevelStr.isEmpty()){
			return;
		}
		Level level = null;
		try {
			level = Level.parse(logLevelStr.toUpperCase());
		} catch (Exception e) {
			log.severe("Failed to parse log level from configuration file due to " + e.getMessage());
			return;
		}
		Logger logger = LogManager.getLogManager().getLogger("");
		logger.setLevel(level);
		for (Handler h : logger.getHandlers()){
			h.setLevel(level);
		}
	}

	public enum DifidoOptions implements ConfigOptions {
		// @formatter:off
		REPORTER_CLASSES("reporter.classes", LocalDifidoReporter.class.getName() +";" + RemoteDifidoReporter.class.getName() +";" +ConsoleReporter.class.getName()),
		MIN_TIME_BETWEEN_WRITES("min.time.between.writes","100"),
		LOG_LEVEL("log.level","INFO"),
		REPORT_PACKAGE_NAMES("report.package.names","true");
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
