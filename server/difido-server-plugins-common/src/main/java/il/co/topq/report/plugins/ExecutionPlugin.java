package il.co.topq.report.plugins;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.business.execution.ExecutionMetadata;

public interface ExecutionPlugin extends Plugin {

	/**
	 * For calling at the end of the execution
	 * 
	 * @param metadata The metadata of the execution
	 * @param execution The execution object
	 */
	void onExecutionEnded(ExecutionMetadata metadata, Execution execution);
}
