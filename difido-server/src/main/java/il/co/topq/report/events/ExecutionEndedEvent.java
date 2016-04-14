package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class ExecutionEndedEvent extends AbsMetadataEvent {

	public ExecutionEndedEvent(ExecutionMetadata executionMetadata) {
		super(executionMetadata);
	}

}
