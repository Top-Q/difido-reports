package il.co.topq.difido;

import org.testng.Assert;
import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestStatuses extends AbstractDifidoTestCase {

	@Test
	public void testWithException() throws Exception {
		report.log("About to fail");
		throw new Exception("This is my failure");
	}
	
	@Test
	public void testWithFailure() throws Exception {
		report.log("About to fail with status failure");
		report.log("Failing", Status.failure);
	}
	
	@Test
	public void testWithFailureToAssertion(){
		report.log("About to fail due to assertion");
		Assert.assertNotNull(null, "Assertion failed due to null");
	}
	
	@Test
	public void testWithAssertionError() {
		report.log("About to fail with assertion error");
		throw new AssertionError("Failing with assertion error");
	}

	@Test
	public void testWithError() throws Exception {
		report.log("Message with error", "Error message", Status.error);
	}

	@Test
	public void testWithWarning() throws Exception {
		report.log("Message with warning", "Warning message", Status.warning);
	}
	
	public void testSuccess(){
		report.log("Everything's is A-OK");
	}



}
