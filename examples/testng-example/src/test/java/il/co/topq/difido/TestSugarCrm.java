package il.co.topq.difido;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestSugarCrm extends AbstractDifidoTestCase {

	private int stepNum;

	@BeforeMethod
	public void setup() {
		report.log("Initializing webdriver");
	}

	@Test
	public void testCreateNewLoad() {
		step("Navigating to login screen");
		addScreenshotFromResource("login.png");
		sendKeys("userTb", "admin");
		sendKeys("passwordTb", "12345");
		click("submitBtn");
		addScreenshotFromResource("dashboard.png");
		step("Navigating to create lead page");
		click("leadsItm");
		addScreenshotFromResource("searchleads.png");
		click("createLeadItm");
		addScreenshotFromResource("createlead.png");
		select("salutationSelect", "Mr.");
		sendKeys("firstNameTb", "Itai");
		sendKeys("lastNameTb", "Agmon");
		click("submitBtn");
		addScreenshotFromResource("saveleads.png");
		step("Asserting that the lead exists");
		addScreenshotFromResource("searchleads2.png");

	}

	private void select(String compName, String option) {
		report.log("Selecting '" + option + "' in component '" + compName + "'");
	}

	private void step(String message) {
		report.step("Step " + ++stepNum + " - " + message);
	}

	private void click(String compName) {
		report.log("Clicking on component " + compName);
	}

	private void sendKeys(String compName, String value) {
		report.log("Sending keys with value '" + value + "' to component '" + compName + "'");
	}

	private File getResource(String resourceName) {
		final File file = new File(getClass().getClassLoader().getResource("login.png").getFile());
		return file;
	}

	private void addScreenshotFromResource(String resourceName) {
		final File file = getResource(resourceName);
		final File fileCopy = new File(System.currentTimeMillis() + file.getName());
		try {
			FileUtils.copyFile(file, fileCopy);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			report.addImage(fileCopy, file.getName().replace(".png", ""));
		} finally {
			fileCopy.delete();
		}

	}

	@AfterMethod
	public void tearDown() {
		report.log("Closing webdriver");
	}

}
