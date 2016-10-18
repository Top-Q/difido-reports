package il.co.topq.difido;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestManyTests extends AbstractDifidoTestCase {
	int numOfTests = 100;

	@Test(description = "Test that repeats a lot", dataProvider = "repeat")
	public void testRepeat(int index) {
		report.log("Running test for the " + index + " time");
		report.addTestProperty("index", index + "");
		report.addRunProperty("Index", index + "");
	}

	@DataProvider(name = "repeat")
	public Object[][] loginDataProvider() {
		Object[][] data = new Object[numOfTests][];
		for (int i = 0 ; i < numOfTests ; i++){
			data[i] = new Object[] { i };
		}
		return data;
	}
}
