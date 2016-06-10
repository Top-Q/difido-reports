package il.co.topq.difido;

import org.junit.Test;

import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

public class TestsWithProperties extends SystemTestCase4 {

	@Test
	@TestProperties(name = "Test that adds test properties")
	public void testAddTestProperties() {
		report.addProperty("int_i", "232");
		report.addProperty("float_f", "23.43");
		report.addProperty("text", "foo bar");
		report.addProperty("date_d", "1978/10/16 16:23:23");
	}

	@Test
	@TestProperties(name = "Test that adds run properties")
	public void testAddRunProperties() {
		report.setContainerProperties(0, "Version", "1.02.3-RC");
		report.setContainerProperties(0, "Build", "46");
		report.setContainerProperties(0, "User", "Itai");
		report.setContainerProperties(0, "Operating System", System.getProperty("os.name"));
		report.setContainerProperties(0, "Java Runtime", System.getProperty("java.runtime.name"));
		report.setContainerProperties(0, "Java Version", System.getProperty("java.runtime.version"));
	}
}
