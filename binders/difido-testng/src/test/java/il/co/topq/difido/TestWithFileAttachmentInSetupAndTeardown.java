package il.co.topq.difido;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestWithFileAttachmentInSetupAndTeardown extends AbstractTestCase {

	private static int index = 0;

	@BeforeMethod
	public void setup() throws IOException {
		report.log("In setup phase");
		addFile();
		addImage("login.png", "Screenshot " + index);

	}

	private void addFile() throws IOException {
		File file = File.createTempFile("file_num_" + index++ + "_", ".txt");
		FileUtils.write(file, "Some data");
		report.addFile(file, "Text file " + index);
	}

	@AfterMethod
	public void teardown() throws IOException {
		report.log("In teardown phase");
		addFile();
		addImage("login.png", "Screenshot " + index);
	}

	private void addImage(String resourceName, String description) {
		final File file = new File(getClass().getClassLoader().getResource(resourceName).getFile());
		File fileCopy = null;
		try {
			fileCopy = File.createTempFile(file.getName(), ".png");
		} catch (IOException e1) {
		}

		try {
			FileUtils.copyFile(file, fileCopy);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		report.addImage(fileCopy, description);
	}

	@Test
	public void test01() throws IOException {
		report.log("In test 01");
	}

	@Test
	public void test02() {
		report.log("In test 02");
	}

}
