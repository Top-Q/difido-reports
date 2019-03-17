package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class ExecutionDeletedEvent extends AbsMetadataEvent {

	private final boolean deleteFromElastic;

	public ExecutionDeletedEvent(int executionId, ExecutionMetadata executionMetaData) {
		this(executionId,executionMetaData,true);
	}

	public ExecutionDeletedEvent(int executionId, ExecutionMetadata executionMetaData, boolean deleteFromElastic) {
		super(executionMetaData);
		this.deleteFromElastic = deleteFromElastic;
	}

	public boolean isDeleteFromElastic() {
		return deleteFromElastic;
	}
	
	

}
