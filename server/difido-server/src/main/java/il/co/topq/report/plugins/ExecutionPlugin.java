package il.co.topq.report.plugins;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface ExecutionPlugin extends Plugin{
	
	void onExecutionEnded(ExecutionMetadata metadata);
	
}
