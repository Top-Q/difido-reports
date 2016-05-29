package il.co.topq.difido;

import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestStatuses extends AbstractDifidoTestCase {

	@Test
	public void testWithFailure() throws Exception {
		report.log("About to fail");
		throw new Exception("This is my failure");
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
