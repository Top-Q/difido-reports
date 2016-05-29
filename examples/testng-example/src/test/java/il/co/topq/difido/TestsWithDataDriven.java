package il.co.topq.difido;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestsWithDataDriven extends AbstractDifidoTestCase {

	@Test(description = "Test login with data provider", dataProvider = "login")
	public void testLogin(String user, String password) {
		report.log("Trying to login with user " + user + " and password " + password);
	}

	@DataProvider(name = "login")
	public Object[][] loginDataProvider() {
		Object[][] data = new Object[2][];
		data[0] = new Object[]{"John","s3cret"};
		data[1] = new Object[]{"Jane","12345"};
		return data;
	}

}
