package il.co.topq.difido;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestsWithSetupAndTeardownLogs extends AbstractTestCase {

	@BeforeMethod
	public void setup() {
		report.log("In the setup phase");
	}

	@Test
	public void testThatSucceeded() {
		report.log("test0");
	}

	@Test
	public void testThatFails() {
		Assert.assertNotNull("Throwing assert error deliberately", null);
	}

	@AfterMethod
	public void tearDown() {
		report.log("In the teardown phase");
	}

}
