package il.co.topq.difido;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestsWithDifferentStatuses extends AbstractDifidoTestCase {
	
	@BeforeMethod
	public void setup(){
		System.out.println("In the setup phase");
	}
	
	@Test(description = "Test with exception")
	public void testWithException() throws Exception {
		report.log("About to fail");
		throw new Exception("This is my failure");
	}

	@Test(description = "Test with failure")
	public void testWithFailureMessage() throws Exception {
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
	public void testWithErrorMessage() throws Exception {
		report.log("Message with error", "Error message", Status.error);
	}
	
	@Test(description = "Test with failure messages")
	public void testWithFailureMessages() {
		report.log("Title without message",Status.failure);
		report.log("Error title 0","Error message 0",Status.failure);
		report.log("Error title 1","Error message 1",Status.failure);
	}

	@Test(description = "Test with warning")
	public void testWithWarning() throws Exception {
		report.log("Message with warning", "Warning message", Status.warning);
	}

	@Test(description = "Test that simply ends with success")
	public void testSuccess() {
		report.log("Everything's is A-OK");
	}
	
	@AfterMethod
	public void tearDown(){
		System.out.println("In the teardown phase");
	}

}
