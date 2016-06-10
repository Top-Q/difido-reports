package il.co.topq.difido;

import org.junit.Test;

import il.co.topq.difido.model.Enums.Status;
import jsystem.framework.TestProperties;
import junit.framework.Assert;
import junit.framework.SystemTestCase4;

public class TestsWithDifferentStatuses extends SystemTestCase4 {

	@Test
	@TestProperties(name = "Test with exception")
	public void testWithException() throws Exception {
		report.report("About to fail");
		throw new Exception("This is my failure");
	}

	@Test
	@TestProperties(name = "Test with failure")
	public void testWithFailure() throws Exception {
		report.report("About to fail with status failure");
		report.report("Failing", false);
	}

	@Test
	@TestProperties(name = "Test with failure due to assertion")
	public void testWithFailureDueToAssertion() {
		report.report("About to fail due to assertion");
		Assert.assertNotNull(null, "Assertion failed due to null");
	}

	@Test
	@TestProperties(name = "Test with assertion error")
	public void testWithAssertionError() {
		report.report("About to fail with assertion error");
		throw new AssertionError("Failing with assertion error");
	}

	@Test
	@TestProperties(name = "Test with error")
	public void testWithError() throws Exception {
		report.report("Message with error", "Error message", false);
	}

	@Test
	@TestProperties(name = "Test with warning")
	public void testWithWarning() throws Exception {
		report.report("Message with warning", "Warning message", 2);
	}

	@Test
	@TestProperties(name = "Test that simply ends with success")
	public void testSuccess() {
		report.report("Everything's is A-OK");
	}

}
