package il.co.topq.report.plugins;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface UpdatePlugin extends Plugin{

	void onMachineCreated(ExecutionMetadata metadata);

}
