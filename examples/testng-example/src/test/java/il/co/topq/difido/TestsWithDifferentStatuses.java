package il.co.topq.difido;

import org.testng.Assert;
import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestsWithDifferentStatuses extends AbstractDifidoTestCase {

	@Test(description = "Test with exception")
	public void testWithException() throws Exception {
		report.log("About to fail");
		throw new Exception("This is my failure");
	}

	@Test(description = "Test with failure")
	public void testWithFailure() throws Exception {
		report.log("About to fail with status failure");
		report.log("Failing", Status.failure);
	}

	@Test(description = "Test with failure due to assertion")
	public void testWithFailureDueToAssertion() {
		report.log("About to fail due to assertion");
		Assert.assertNotNull(null, "Assertion failed due to null");
	}

	@Test(description = "Test with assertion error")
	public void testWithAssertionError() {
		report.log("About to fail with assertion error");
		throw new AssertionError("Failing with assertion error");
	}

	@Test(description = "Test with error")
	public void testWithError() throws Exception {
		report.log("Message with error", "Error message", Status.error);
	}

	@Test(description = "Test with warning")
	public void testWithWarning() throws Exception {
		report.log("Message with warning", "Warning message", Status.warning);
	}

	@Test(description = "Test that simply ends with success")
	public void testSuccess() {
		report.log("Everything's is A-OK");
	}

}
