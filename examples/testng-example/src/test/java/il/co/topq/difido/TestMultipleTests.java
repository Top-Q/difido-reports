package il.co.topq.difido;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestMultipleTests extends AbstractDifidoTestCase {

	private int numOfTests = 100;

	@Test(description = "Test that repeates many time", dataProvider = "data")
	public void testTheRepeatesManyTimes(int index) {
		report.log("Test number " + index);
	}

	@DataProvider(name = "data")
	public Object[][] dataProvider() {
		Object[][] data = new Object[numOfTests][];
		for (int i = 0; i < numOfTests; i++) {
			data[i] = new Object[] { i };
		}
		return data;
	}
}
