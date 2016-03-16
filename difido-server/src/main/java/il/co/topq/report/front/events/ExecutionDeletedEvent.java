package il.co.topq.report.front.events;

import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

public class ExecutionDeletedEvent extends AbsMetadataEvent{

	private final int executionId;

	public ExecutionDeletedEvent(int executionId, ExecutionMetadata executionMetaData) {
		super(executionMetaData);
		this.executionId = executionId;
	}

	public int getExecutionId() {
		return executionId;
	}

}
