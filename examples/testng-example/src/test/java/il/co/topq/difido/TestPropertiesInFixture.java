package il.co.topq.difido;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPropertiesInFixture extends AbstractDifidoTestCase {

	@BeforeMethod
	public void setup() throws Exception {
		report.addRunProperty("runProp0", "runPropValue0");
		report.addRunProperty("runProp1", "runPropValue1");
		report.addTestProperty("testProp0", "testValue0");
		report.addTestProperty("testProp1", "testValue1");
	}

	@Test(description = "Test with test and run properties that were added in the before method phase")
	public void testWithPropertiesThatWereAddedInFixture() {
		report.log("In test");
	}

}
