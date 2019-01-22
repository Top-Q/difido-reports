package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class ExecutionDeletedEvent extends AbsMetadataEvent {

	public ExecutionDeletedEvent(int executionId, ExecutionMetadata executionMetaData) {
		super(executionMetaData);
	}

}
