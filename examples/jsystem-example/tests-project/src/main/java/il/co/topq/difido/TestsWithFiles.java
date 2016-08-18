package il.co.topq.difido;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import jsystem.framework.TestProperties;
import jsystem.framework.report.ReporterHelper;
import junit.framework.SystemTestCase4;

public class TestsWithFiles extends SystemTestCase4 {

	@Test
	@TestProperties(name = "Adding text file to the report and adding link")
	public void testAddWithLinkFile() throws Exception {
		File file = createFile();
		try {
			ReporterHelper.copyFileToReporterAndAddLink(report, file, "A general text file");
		} finally {
			file.delete();
		}
	}

	@Test
	@TestProperties(name = "Adding text file to the report and adding property")
	public void testAddFileWithProperty() throws IOException {
		File file = createFile();
		try {
			report.saveFile(file.getName(), FileUtils.readFileToByteArray(file));
			report.addProperty("file", "<a href='" + file.getName() + "'>" + file.getName() + "</a>");
		} finally {
			file.delete();
		}
	}

	private File createFile() throws IOException {
		String content = "Lorem ipsum dolor sit amet, "
				+ "consectetur adipiscing elit. Nunc id justo ac lacus finibus venenatis"
				+ ". Nunc imperdiet nunc purus, non aliquam mi sagittis et. "
				+ "Suspendisse metus erat, interdum at tincidunt vel, "
				+ "euismod fermentum ipsum. Ut sapien arcu, tempus et ultricies a, "
				+ "aliquet id elit. Phasellus et libero non lorem auctor malesuada "
				+ "feugiat sed lectus. Morbi faucibus scelerisque felis, scelerisque volutpat risus "
				+ "dapibus ac. Morbi nec sem aliquet, feugiat ex id, porttitor purus. "
				+ "Duis mattis volutpat orci quis lobortis. Interdum et malesuada fames ac ante ipsum primis in faucibus. "
				+ "Morbi eu ex felis. Donec semper interdum eros, sed varius dolor finibus in. "
				+ "Curabitur neque est, iaculis non quam in, posuere pellentesque nunc. "
				+ "Mauris purus diam, pharetra et justo eget, congue tristique purus. "
				+ "In vulputate mi justo, in volutpat magna porttitor a.";
		File tempFile = File.createTempFile("tempTestingFile", ".txt");
		FileUtils.write(tempFile, content);
		return tempFile;
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
