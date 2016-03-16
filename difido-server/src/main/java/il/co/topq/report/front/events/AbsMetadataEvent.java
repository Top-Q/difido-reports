package il.co.topq.report.front.events;

import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

public abstract class AbsMetadataEvent implements MetadataEvent {

	private final ExecutionMetadata metadata;

	public AbsMetadataEvent(ExecutionMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public ExecutionMetadata getMetadata() {
		return metadata;
	}

}
