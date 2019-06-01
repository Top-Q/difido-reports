package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

/**
 * @author itai
 *
 */
public class ExecutionCreatedEvent extends AbsMetadataEvent {

	public ExecutionCreatedEvent(ExecutionMetadata executionMetaData) {
		super(executionMetaData.getId());
	}

}
