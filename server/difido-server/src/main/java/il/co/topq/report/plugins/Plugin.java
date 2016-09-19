package il.co.topq.report.plugins;

public interface Plugin {

	/**
	 * The name of the plugin. Mostly used for manually execution
	 * 
	 * @return The name of the plugin
	 */
	public String getName();

	/**
	 * For manual triggering
	 * 
	 * @param params
	 *            Open string for any parameter that the plugin require
	 */
	void execute(String params);

}
