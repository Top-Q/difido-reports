package il.co.topq.difido.config;


public class GenericBinderConfig extends AbstractPropertiesConfigFile{

	public GenericBinderConfig() {
		super(BinderOptions.values());
	}


	@Override
	protected String getFileName() {
		return "config.properties";
	}

	
	public enum BinderOptions implements ConfigOptions {
		// @formatter:off
		REMOTE_DIFIDO_ENABLED("remote.difido.enabled","true"),
		LOCAL_DIFIDO_ENABLED("local.difido.enabled","true"),
		REMOTE_DIFIDO_HOST("remote.difido.host","localhost"),
		REMOTE_DIFIDO_PORT("remote.difido.host","8080"),
		BINDER_CLASS("binder.class","il.co.topq.difido.binder.JUnitXmlBinder"),
		SOURCE("source",""),
		DESTINATION_FOLDER("destination.folder","difido");
		// @formatter:on

		private String property;

		private String defaultValue;

		private BinderOptions(final String property, final String defaultValue) {
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
			return BinderOptions.values();
		}		
	}
}
