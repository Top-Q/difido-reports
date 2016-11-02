package il.co.topq.difido;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.testng.annotations.Test;

public class TestsWithFiles extends AbstractDifidoTestCase {

	@Test(description = "Adding the pom file to the report")
	public void testAddFile() {
		File file = new File("pom.xml");
		report.addFile(file, "This is the file");
	}

	@Test(description = "Adding screenshot to the report")
	public void testAddScreenshot0() throws IOException, AWTException {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Robot robot = new Robot();
		BufferedImage image = robot.createScreenCapture(screenRectangle);
		File imgFile = File.createTempFile("screenshot_file", "png");
		ImageIO.write(image, "png", imgFile);
		report.addImage(imgFile, "My screenshot file");
		imgFile.delete();

	}
	
	@Test(description = "Adding screenshot to the report")
	public void testAddScreenshot1() throws IOException, AWTException {
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
