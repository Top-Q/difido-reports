package il.co.topq.difido;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsystem.framework.TestProperties;
import jsystem.framework.report.ReporterHelper;
import junit.framework.Assert;
import junit.framework.SystemTestCase4;

public class TestsSugarCrm extends SystemTestCase4 {

	private int stepNum;

	@Before
	public void setup() {
		stepNum = 0;
	}

	@Test
	@TestProperties(name = "Test creation of a new lead", group = { "sugarcrm", "regression" })
	public void testCreateNewLead() throws Exception {
		testBody(false);
		Assert.assertEquals("Lead 'Mr. Itai Agmon' was found", "Lead 'Mr. Itai Agmon' was found");
	}

	@Test
	@TestProperties(name = "Test creation of a new lead", group = { "sugarcrm", "regression" })
	public void testCreateNewLeadAndFail() throws Exception {
		testBody(false);
		Assert.assertEquals("No lead 'Mr. Itai Agmon' was found", "Lead 'Mr. Itai Agmon' was found");
	}

	@Test
	@TestProperties(name = "Test creation of a new lead", group = { "sugarcrm", "regression" })
	public void testCreateLeadWithWrongSelector() throws Exception {
		testBody(true);
	}

	private void testBody(boolean failInStep) throws Exception {
		step("Navigating to login screen");
		addScreenshotFromResource("login.png");
		report.startLevel("Performing login");
		sendKeys("userTb", "admin");
		sendKeys("passwordTb", "12345");
		click("submitBtn");
		report.stopLevel();

		addScreenshotFromResource("dashboard.png");
		step("Navigating to create lead page");
		report.startLevel("Clicking on 'Leads' menu item");
		click("SalesItm");
		click("LeadsItm");
		report.stopLevel();

		addScreenshotFromResource("searchleads.png");
		report.startLevel("Clicking on create new lead");
		click("createLeadItm");
		report.stopLevel();

		addScreenshotFromResource("createlead.png");
		report.startLevel("Typing lead details");
		select("salutationSelect", "Mr.");
		sendKeys("firstNameTb", "Itai");
		if (failInStep) {
			throw new Exception("Element not found exception");

		}
		sendKeys("lastNameTb", "Agmon");
		click("submitBtn");
		report.stopLevel();

		addScreenshotFromResource("saveleads.png");
		step("Asserting that the lead exists");
		report.report("SELECT key,salutation,first_name,last_name FROM Leads WHERE last_name='Agmon';", getHtmlTable(),
				true);
		ReporterHelper.copyFileToReporterAndAddLink(report, getResource("server.log"), "Server log file");
		addScreenshotFromResource("searchleads2.png");
	}

	private static String getHtmlTable() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<table style=\"width:100%\"  border=\"1\">");
		sb.append("	<tr>");
		sb.append("		<th>key</th>");
		sb.append("		<th>salutation</th>");
		sb.append("		<th>first_name</th> ");
		sb.append("		<th>last_name</th>");
		sb.append("	</tr>");
		sb.append("	<tr>");
		sb.append("		<td>34523</td>");
		sb.append("		<td>Mr.</td>");
		sb.append("		<td>Itai</td> ");
		sb.append("		<td>Agmon</td>");
		sb.append("	</tr>");
		sb.append("</table>");
		return sb.toString();
	}

	private void select(String compName, String option) {
		report.report("Selecting '" + option + "' in component '" + compName + "'");
	}

	private void step(String message) {
		report.step("Step " + ++stepNum + " - " + message);
	}

	private void click(String compName) {
		report.report("Clicking on component " + compName);
	}

	private void sendKeys(String compName, String value) {
		report.report("Sending keys with value '" + value + "' to component '" + compName + "'");
	}

	private File getResource(String resourceName) {
		final File file = new File(getClass().getClassLoader().getResource(resourceName).getFile());
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
			ReporterHelper.copyFileToReporterAndAddLink(report, fileCopy, file.getName().replace(".png", ""));
		} catch (Exception e) {
		} finally {
			fileCopy.delete();
		}

	}

	@After
	public void tearDown() {
		report.step("Teardown");
		report.report("Closing webdriver");
	}

}
