package il.co.topq.difido;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class TestsWithFailedTeardown extends AbstractTestCase {

	@AfterMethod
	public void teardown() throws Exception {
		report.log("In the teardown phase. About to throw exception");
		throw new Exception("Throwing exception deliberately");
	}

	@Test
	public void testThatSucceeded() {
		report.log("In test");
	}

	@Test
	public void testThatFails() {
		Assert.assertNotNull("Throwing assert error deliberately", null);
	}

}
