package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

/**
 * @author itai
 *
 */
public class ExecutionCreatedEvent extends AbsMetadataEvent {

	private final ExecutionMetadata executionMetaData;

	public ExecutionCreatedEvent(ExecutionMetadata executionMetaData) {
		super(executionMetaData.getId());
		this.executionMetaData = executionMetaData;
	}

	public ExecutionMetadata getExecutionMetaData() {
		return executionMetaData;
	}

}
