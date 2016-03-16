package il.co.topq.report.front.events;

import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

public interface MetadataEvent {

	ExecutionMetadata getMetadata();

}
