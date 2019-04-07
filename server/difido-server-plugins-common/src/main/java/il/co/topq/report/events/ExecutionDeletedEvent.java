package il.co.topq.report.events;

public class ExecutionDeletedEvent extends AbsMetadataEvent {

	private final boolean deleteFromElastic;

	public ExecutionDeletedEvent(int executionId) {
		this(executionId, true);
	}

	public ExecutionDeletedEvent(int executionId, boolean deleteFromElastic) {
		super(executionId);
		this.deleteFromElastic = deleteFromElastic;
	}

	public boolean isDeleteFromElastic() {
		return deleteFromElastic;
	}

}
