package il.co.topq.difido;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestsThatAreSkipped extends AbstractDifidoTestCase {
	
	@BeforeMethod
	public void setup() throws Exception{
		report.log("Something bad is going to happen at the setup phase");
		throw new Exception("Something bad happened");
	}
	
	@Test
	public void supposedToBeSkippedTest(){
		report.log("You should never get to this line");
	}
	
}
