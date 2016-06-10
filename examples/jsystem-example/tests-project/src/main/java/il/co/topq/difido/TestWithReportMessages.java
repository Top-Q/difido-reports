package il.co.topq.difido;

import java.io.IOException;

import org.junit.Test;

import il.co.topq.difido.model.Enums.Status;
import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

public class TestWithReportMessages extends SystemTestCase4 {

	@Test
	@TestProperties(name = "Test with steps")
	public void testWithSteps() {
		report.step("Step 1");
		report.report("Simple log message");
		report.step("Step 2");
		report.step("Step 3");
	}

	@Test
	@TestProperties(name = "Test with levels")
	public void testWithLevels() throws IOException {
		report.startLevel("Click me to see the messages");
		report.report("Message in level");
		report.report("Message in level");
		report.report("Message in level");
		report.report("Message in level");
		report.stopLevel();
	}

	@Test
	@TestProperties(name = "Test with multiple levels")
	public void testWithMultipleLevels() throws IOException {
		report.startLevel("Level 1");
		try {
			report.report("Message in level 1");
			report.startLevel("Level 2");
			try {
				report.report("Message in level 2");
				report.startLevel("Level 3");
				try {
					report.report("Message in level 3");
				} finally {
					report.stopLevel();
				}
			} finally {
				report.stopLevel();
			}
			report.report("Message in level 1");

		} finally {
			report.stopLevel();

		}

	}

	@Test
	@TestProperties(name = "Test with multiple levels with failures")
	public void testWithMultipleLevelsWithFailures() throws IOException {
		report.startLevel("Level 1");
		try {
			report.report("Message in level 1");
			report.startLevel("Level 2");
			try {
				report.report("Message in level 2");
				report.startLevel("Level 3");
				try {
					report.report("Message in level 3");
					report.report("Failure", 2);
				} finally {
					report.stopLevel();
				}

				report.startLevel("Level 3 (again)");
				try {
					report.report("Message in level 3");
					report.report("Success");
				} finally {
					report.stopLevel();
				}

			} finally {
				report.stopLevel();
			}
			try {
				report.startLevel("Level 2 (again)");
				report.report("Message in level 2");
			} finally {
				report.stopLevel();
			}

			report.report("Message in level 1");

		} finally {
			report.stopLevel();

		}
		report.startLevel("Level 1 (2)");
		try {
			report.report("In level 1 (2)");
			report.startLevel("Level 2 (2)");
			try {
				report.report("In level 2 (2)");
			} finally {
				report.stopLevel();
			}

		} finally {
			report.stopLevel();
		}

	}

	@Test
	@TestProperties(name = "Test with various log messages")
	public void testWithVariousLogMessages() throws Exception {
		report.step("This is the first step");
		report.startLevel("Starting level");
		report.report("Message inside level");
		report.report("This is title", "this is message", true);
		report.report("Message inside level", "Inside level", true);
		report.report("Message inside level", "Inside level", true);
		report.stopLevel();

		report.step("This is the second step");
		report.startLevel("Level with failure");
		report.report("Something wrong happened", false);
		report.stopLevel();
	}

}
