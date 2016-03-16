package il.co.topq.report.front.events;

import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

public class ExecutionEndedEvent extends AbsMetadataEvent{
	
	private final int executionId;

	public ExecutionEndedEvent(int executionId, ExecutionMetadata executionMetadata) {
		super(executionMetadata);
		this.executionId = executionId;
	}

	public int getExecutionId() {
		return executionId;
	}
	
	
	
	
	
	
}
