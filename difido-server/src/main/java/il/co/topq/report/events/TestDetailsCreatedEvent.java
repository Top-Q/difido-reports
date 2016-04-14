package il.co.topq.report.events;

import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.ExecutionMetadata;

public class TestDetailsCreatedEvent extends AbsMetadataEvent {

	private final TestDetails testDetails;

	public TestDetailsCreatedEvent(ExecutionMetadata executionMetadata, TestDetails testDetails) {
		super(executionMetadata);
		this.testDetails = testDetails;
	}

	public TestDetails getTestDetails() {
		return testDetails;
	}

}
