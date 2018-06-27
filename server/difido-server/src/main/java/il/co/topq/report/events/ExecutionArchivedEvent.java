package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class ExecutionArchivedEvent extends AbsMetadataEvent {

	public ExecutionArchivedEvent(ExecutionMetadata metadata) {
		super(metadata);
	}

}
