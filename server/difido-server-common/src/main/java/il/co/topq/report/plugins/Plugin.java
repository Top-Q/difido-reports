package il.co.topq.report.plugins;

import java.util.List;

import il.co.topq.report.business.execution.ExecutionMetadata;

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
	 * 
	 * @param List
	 *            of the execution metaData on which the plugin should be
	 *            operated on.
	 */
	void execute(List<ExecutionMetadata> metaDataList, String params);

}
