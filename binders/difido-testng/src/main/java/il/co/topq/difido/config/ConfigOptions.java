package il.co.topq.difido.config;

public interface ConfigOptions {
	
	String getProperty();

	String getDefaultValue();
	
	ConfigOptions[] getOptions();

}
