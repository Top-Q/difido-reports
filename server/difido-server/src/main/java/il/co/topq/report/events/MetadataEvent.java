package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface MetadataEvent {

	ExecutionMetadata getMetadata();

	int getExecutionId();

}
