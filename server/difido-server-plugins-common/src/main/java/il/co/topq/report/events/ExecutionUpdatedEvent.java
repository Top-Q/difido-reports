package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class ExecutionUpdatedEvent extends AbsMetadataEvent {

	public ExecutionUpdatedEvent(ExecutionMetadata metadata) {
		super(metadata);
	}

}
