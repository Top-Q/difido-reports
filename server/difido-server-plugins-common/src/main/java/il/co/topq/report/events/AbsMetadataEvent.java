package il.co.topq.report.events;

public abstract class AbsMetadataEvent implements MetadataEvent {

	final private int executionId;
	
	public AbsMetadataEvent(int executionId) {
		this.executionId = executionId;
	}

	@Override
	public int getExecutionId() {
		return executionId;
	}

}
