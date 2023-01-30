package il.co.topq.report.plugins;

import java.util.List;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.business.execution.ExecutionMetadata;

public interface InteractivePlugin extends Plugin {
	
	/**
	 *
	 * @param executions
	 * 			 List of all execution to activate the plugin on
	 *
	 * @param params
	 *            Open string for any parameter that the plugin require
	 *
	 * @param metaDataList
	 *            of the execution metaData on which the plugin should be
	 *            operated on.
	 * @return HTML String with the plugin response
	 */
	String executeInteractively(List<ExecutionMetadata> metaDataList, List<Execution> executions, String params);
	
}
