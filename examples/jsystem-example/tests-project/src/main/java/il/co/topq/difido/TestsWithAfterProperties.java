package il.co.topq.difido;

import org.junit.After;
import org.junit.Test;

import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

public class TestsWithAfterProperties extends SystemTestCase4 {

	@After
	public void setUp(){
		report.setContainerProperties(0, "setup", "FINDME");
	}

	
	@Test
	@TestProperties(name = "Test that adds run properties")
	public void testAddRunProperties() {
		report.report("Check the teardown");
	}
	
	@After
	public void tearDown(){
		report.setContainerProperties(0, "teardown", "FINDME");
		
	}
}
