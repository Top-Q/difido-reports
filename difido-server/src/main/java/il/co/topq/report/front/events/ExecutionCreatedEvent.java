package il.co.topq.report.front.events;

import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

/**
 * @author itai
 *
 */
public class ExecutionCreatedEvent extends AbsMetadataEvent {

	private final int executionId;

	public ExecutionCreatedEvent(int executionId, ExecutionMetadata executionMetaData) {
		super(executionMetaData);
		this.executionId = executionId;
	}

	public int getExecutionId() {
		return executionId;
	}

}
