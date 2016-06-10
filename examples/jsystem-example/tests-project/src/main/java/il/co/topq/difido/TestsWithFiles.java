package il.co.topq.difido;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;

import jsystem.framework.TestProperties;
import jsystem.framework.report.ReporterHelper;
import junit.framework.SystemTestCase4;

public class TestsWithFiles extends SystemTestCase4 {

	@Test
	@TestProperties(name = "Adding the pom file to the report")
	public void testAddFile() throws Exception {
		File file = new File("pom.xml");
		ReporterHelper.copyFileToReporterAndAddLink(report, file, "This is the POM file of the project");
	}

	@Test
	@TestProperties(name = "Adding screenshot to the report")
	public void testAddScreenshot() throws Exception {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Robot robot = new Robot();
		BufferedImage image = robot.createScreenCapture(screenRectangle);
		File imgFile = File.createTempFile("screenshot_file", "png");
		ImageIO.write(image, "png", imgFile);
		ReporterHelper.copyFileToReporterAndAddLink(report, imgFile, "My screenshot file");
		imgFile.delete();

	}

}
