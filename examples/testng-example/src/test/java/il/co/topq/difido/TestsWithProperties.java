package il.co.topq.difido;

import org.testng.annotations.Test;

public class TestsWithProperties extends AbstractDifidoTestCase {
		
	@Test(description = "Test that adds test properties")
	public void testAddTestProperties(){
		report.addTestProperty("int_i", "232");
		report.addTestProperty("long_l", "23223435453");
		report.addTestProperty("double_d", "23.489893");
		report.addTestProperty("float_d", "23.48");
		report.addTestProperty("text", "foo bar");
		report.addTestProperty("date_d", "1978/10/16 16:23:23");
	}
	
	@Test(description = "Test that adds run properties")
	public void testAddRunProperties(){
		report.addRunProperty("Version", "1.02.3-RC");
		report.addRunProperty("Build", "46");
		report.addRunProperty("User", "Itai");
		report.addRunProperty("Operating System", System.getProperty("os.name"));
		report.addRunProperty("Java Runtime", System.getProperty("java.runtime.name"));
		report.addRunProperty("Java Version", System.getProperty("java.runtime.version"));
	}
}