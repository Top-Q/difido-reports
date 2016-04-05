package il.co.topq.report.events;

import il.co.topq.report.business.execution.MetadataController.ExecutionMetadata;

public interface MetadataEvent {

	ExecutionMetadata getMetadata();

}
