package il.co.topq.report.events;

import il.co.topq.report.business.execution.MetadataController.ExecutionMetadata;

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
