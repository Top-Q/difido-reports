package il.co.topq.difido;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestWithErrorLogMessageInSetupAndTeardown extends AbstractTestCase {

	@BeforeMethod
	public void setup() {
		report.log("This is a failure message that should be changed to warning", Status.failure);
		report.log("This is an error message that should be changed to warning", Status.error);
	}

	@Test
	public void testThatShouldBeSkipped() {
		report.log("In the test body");
	}

	@AfterMethod
	public void teardown() {
		report.log("This is a failure message that should be changed to warning", Status.failure);
		report.log("This is an error message that should be changed to warning", Status.error);
	}

}
