package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class ExecutionUpdatedEvent extends AbsMetadataEvent {

	private final ExecutionMetadata executionMetadata;
	
	public ExecutionUpdatedEvent(ExecutionMetadata executionMetadata) {
		super(executionMetadata.getId());
		this.executionMetadata = executionMetadata;
	}

	public ExecutionMetadata getExecutionMetadata() {
		return executionMetadata;
	}

}
