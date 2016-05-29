package il.co.topq.difido;

import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestReportMessages extends AbstractDifidoTestCase {

	@Test(description = "Test with steps")
	public void testWithSteps() {
		report.step("Step 1");
		report.log("Simple log message");
		report.step("Step 2");
		report.step("Step 3");
	}

	@Test(description = "Test with levels")
	public void testWithLevels() {
		report.startLevel("Click me to see the messages");
		report.log("Message in level");
		report.log("Message in level");
		report.log("Message in level");
		report.log("Message in level");
		report.endLevel();
	}

	@Test(description = "Test with multiple levels")
	public void testWithMultipleLevels() {
		report.startLevel("Level 1");
		try {
			report.log("Message in level 1");
			report.startLevel("Level 2");
			try {
				report.log("Message in level 2");
				report.startLevel("Level 3");
				try {
					report.log("Message in level 3");
				} finally {
					report.endLevel();
				}
			} finally {
				report.endLevel();
			}
			report.log("Message in level 1");

		} finally {
			report.endLevel();

		}

	}
	
	@Test(description = "Test with multiple levels with failures")
	public void testWithMultipleLevelsWithFailures() {
		report.startLevel("Level 1");
		try {
			report.log("Message in level 1");
			report.startLevel("Level 2");
			try {
				report.log("Message in level 2");
				report.startLevel("Level 3");
				try {
					report.log("Message in level 3");
					report.log("Failure",Status.error);
				} finally {
					report.endLevel();
				}
				
				report.startLevel("Level 3 (again)");
				try {
					report.log("Message in level 3");
					report.log("Success");
				} finally {
					report.endLevel();
				}

				
			} finally {
				report.endLevel();
			}
			report.log("Message in level 1");

		} finally {
			report.endLevel();

		}

	}


	@Test(description = "Test with various log messages")
	public void testWithVariousLogMessages() throws Exception {
		report.step("This is the first step");
		report.startLevel("Starting level");
		report.log("Message inside level");
		report.log("This is title", "this is message");
		report.log("Message inside level", "Inside level");
		report.log("Message inside level", "Inside level");
		report.endLevel();

		report.step("This is the second step");
		report.startLevel("Level with failure");
		report.log("Something wrong happened", Status.failure);
		report.endLevel();
	}

}
