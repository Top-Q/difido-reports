package il.co.topq.difido;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class TestsWithPrettyNames extends AbstractDifidoTestCase {

	@Test
	public void testWithNoPrettyNameAndNoParams() {

		report.log("This is printed from a test with no pretty name");
		
	}
	
	@Test
	@Parameters({"someParam"})
	public void testWithParamAndNoPrettyName(@Optional("some_param_value") String someParam) {

		report.log("This is printed from a test with no pretty name and with some param = " + someParam);
		
	}
	
	@Test
	@Parameters("a") // parameter names for different tests in the same class must be different because otherwise the parameter value is not being updated between tests
	public void testWithPrettyName1(@Optional("prettyTestName=Test With Pretty Name - No. 1") String prettyTestName) {

		report.log("This is printed from test No. 1 with a pretty name");
		
	}
	
	@Test
	@Parameters("b")  // parameter names for different tests in the same class must be different because otherwise the parameter value is not being updated between tests
	public void testWithPrettyName2(@Optional("prettyTestName=Test With Pretty Name - No. 2") String prettyTestName) {

		report.log("This is printed from test No. 2 with a pretty name");
		
	}
}
