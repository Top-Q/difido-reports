package il.co.topq.report.events;

import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.MetadataController.ExecutionMetadata;

public class TestDetailsCreatedEvent extends AbsMetadataEvent {

	private final int executionId;

	private final TestDetails testDetails;

	public TestDetailsCreatedEvent(int executionId, ExecutionMetadata executionMetadata, TestDetails testDetails) {
		super(executionMetadata);
		this.executionId = executionId;
		this.testDetails = testDetails;
	}

	public int getExecutionId() {
		return executionId;
	}

	public TestDetails getTestDetails() {
		return testDetails;
	}

}
