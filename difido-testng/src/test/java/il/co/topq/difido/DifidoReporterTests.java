package il.co.topq.difido;

import il.co.topq.difido.model.Enums.Status;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(il.co.topq.difido.ReportManagerHook.class)
public class DifidoReporterTests {

	ReportDispatcher report = ReportManager.getInstance();

	@Test(description = "This is my custom description", groups = { "sanity", "regression" })
	public void simpleReportCall0() {
		report.log("some title", "Some message", Status.success);
	}

	@Test
	public void testWithFailure() throws Exception {
		report.log("About to fail");
		throw new Exception("This is my failure");
	}

	@Test
	public void testWithError() throws Exception {
		report.log("Message with error", "Error message", Status.error);
	}

	@Test
	public void testWithWarning() throws Exception {
		report.log("Message with warning", "Warning message", Status.warning);
	}

	@Test
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

	@Test
	public void testAddFile() {
		File file = new File("pom.xml");
		report.addFile(file, "This is the file");
	}

	@Test(description = "Adding screenshot to the report")
	public void testAddScreenshot() throws IOException, AWTException {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Robot robot = new Robot();
		BufferedImage image = robot.createScreenCapture(screenRectangle);
		File imgFile = File.createTempFile("screenshot_file", "png");
		ImageIO.write(image, "png", imgFile);
		report.addImage(imgFile, "My screenshot file");
		imgFile.delete();

	}

}
