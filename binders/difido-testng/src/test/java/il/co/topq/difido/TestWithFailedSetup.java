package il.co.topq.difido;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestWithFailedSetup extends AbstractTestCase {

	@BeforeMethod
	public void setup() throws Exception {
		report.log("In the setup phase. About to throw exception");
		throw new Exception("Throwing exception deliberately");
	}

	@Test
	public void testThatShouldBeSkipped() {
		report.log("Should not shown");
	}

}
