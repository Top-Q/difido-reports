package il.co.topq.report.events;

import il.co.topq.difido.model.test.TestDetails;

public class TestDetailsCreatedEvent extends AbsMetadataEvent {

	private final TestDetails testDetails;

	public TestDetailsCreatedEvent(int executionId, TestDetails testDetails) {
		super(executionId);
		this.testDetails = testDetails;
	}

	public TestDetails getTestDetails() {
		return testDetails;
	}

}
