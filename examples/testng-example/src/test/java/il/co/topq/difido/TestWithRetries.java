package il.co.topq.difido;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestWithRetries extends AbstractDifidoTestCase {
	
	private final static int MAX_NUM_OF_FAILURES = 2;
	private static int numOfFailures;
	
	@Test(retryAnalyzer=MyRetryAnalyzer.class)
	public void naughyNaughtyTest(){
		report.log("In flaky test ");
		if (numOfFailures++ < MAX_NUM_OF_FAILURES) {
			report.log("About to fail test");
			Assert.assertNotNull(null,"Failing test");	
		}
		report.log("Test should pass");
		
		

	}

}
