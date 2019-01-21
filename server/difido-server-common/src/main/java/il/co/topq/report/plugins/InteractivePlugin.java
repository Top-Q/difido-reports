package il.co.topq.report.plugins;

import java.util.List;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface InteractivePlugin extends Plugin {
	
	/**
	 * 
	 * @param metaDataList
	 * @param params
	 * @return HTML String with the plugin response
	 */
	String executeInteractively(List<ExecutionMetadata> metaDataList, String params);
	
}
