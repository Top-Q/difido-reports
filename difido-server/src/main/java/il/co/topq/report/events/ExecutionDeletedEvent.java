package il.co.topq.report.events;

import il.co.topq.report.business.execution.MetadataController.ExecutionMetadata;

public class ExecutionDeletedEvent extends AbsMetadataEvent {

	private final int executionId;

	public ExecutionDeletedEvent(int executionId, ExecutionMetadata executionMetaData) {
		super(executionMetaData);
		this.executionId = executionId;
	}

	public int getExecutionId() {
		return executionId;
	}

}
