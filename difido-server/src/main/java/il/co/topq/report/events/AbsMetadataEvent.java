package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public abstract class AbsMetadataEvent implements MetadataEvent {

	private final ExecutionMetadata metadata;

	public AbsMetadataEvent(ExecutionMetadata metadata) {
		if (null == metadata) {
			throw new IllegalArgumentException("Metadata can't be null");
		}
		this.metadata = metadata;
	}

	@Override
	public ExecutionMetadata getMetadata() {
		return metadata;
	}

	@Override
	public int getExecutionId() {
		return metadata.getId();
	}

}
