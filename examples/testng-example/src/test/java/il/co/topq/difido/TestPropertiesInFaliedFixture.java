package il.co.topq.difido;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPropertiesInFaliedFixture extends AbstractDifidoTestCase {

	@BeforeMethod
	public void setup() {
		report.addRunProperty("runProp0", "runPropValue0");
		report.addRunProperty("runProp1", "runPropValue1");
		report.addTestProperty("testProp0", "testValue0");
		report.addTestProperty("testProp1", "testValue1");
		Assert.assertNotNull(null, "Failing this in purpose");
	}

	@Test(description = "Test with fixture and run properties that were added in the before method phase in failed fixture")
	public void testWithPropertiesThatWereAddedInFixture() {
		report.log("In test");
	}

}
