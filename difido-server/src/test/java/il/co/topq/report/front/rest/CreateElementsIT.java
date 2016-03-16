package il.co.topq.report.front.rest;

import java.io.File;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class CreateElementsIT extends AbstractResourceTest {

	private static final int NUM_OF_REPORTS_ELEMENTS = 50;
	private static final String MACHINE_NAME = "My Machine";
	private static final String SCENARIO_NAME = "My Scenario";
	private static final String TEST_NAME = "My test";
	private static final String TEST_DETAILS_NAME = "My test details";
	private static final long SLEEP_TIME = 1000;
	private TestDetails details;
	private int executionId;
	private String uid;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		final ExecutionDetails description = new ExecutionDetails("Testing", true);
		executionId = client.addExecution(description);
		final MachineNode machine = new MachineNode(MACHINE_NAME);
		final int machineId = client.addMachine(executionId, machine);
		final ScenarioNode scenario = new ScenarioNode(SCENARIO_NAME);
		machine.addChild(scenario);
		final TestNode test = new TestNode(0, TEST_NAME, "0");
		uid = String.valueOf(Math.abs(new Random().nextInt()));
		test.setUid(uid);
		scenario.addChild(test);
		client.updateMachine(executionId, machineId, machine);
		details = new TestDetails(TEST_DETAILS_NAME, "0");
		details.setUid(uid);
	}

	@Test
	public void testAddReportElement() throws Exception {
		ReportElement element = new ReportElement(details);
		element.setType(ElementType.regular);
		element.setTime("00:00");
		final String title = "My report element";
		element.setTitle(title);
		final String message = "My report element message";
		element.setMessage(message);
		details.addReportElement(element);
		client.addTestDetails(executionId, details);

		sleep();
		final TestDetails testDetails = assertExecution();
		element = testDetails.getReportElements().get(0);
		Assert.assertEquals(ElementType.regular, element.getType());
		Assert.assertEquals(title, element.getTitle());
		Assert.assertEquals(message, element.getMessage());

	}

	@Test
	public void measureAddReportElements() throws Exception {
		ReportElement element = null;
		for (int i = 0; i < NUM_OF_REPORTS_ELEMENTS; i++) {
			element = new ReportElement(details);
			element.setType(ElementType.regular);
			element.setTime("00:" + i);
			element.setTitle("My report element " + i);
			details.addReportElement(element);
			long start = System.currentTimeMillis();
			client.addTestDetails(executionId, details);
			System.out.println("Element was added in " + (System.currentTimeMillis() - start) + " millis");
		}
		sleep();
		assertExecution();
		System.out.println("End");

	}

	@Test
	public void testAddFile() throws Exception {
		final File file = new File("src/test/resources/top-q.pdf");
		ReportElement element = new ReportElement(details);
		element.setType(ElementType.lnk);
		element.setTime("00:00");
		element.setTitle("My report element");
		element.setMessage(file.getName());
		details.addReportElement(element);
		client.addTestDetails(executionId, details);
		client.addFile(executionId, uid, file);
		sleep();
		assertExecution();

	}
	
	private static void sleep(){
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private TestDetails assertExecution() {
		final Execution execution = getExecution();
		Assert.assertNotNull(execution);
		final MachineNode machine = execution.getMachines().get(0);
		Assert.assertEquals(MACHINE_NAME, machine.getName());
		final ScenarioNode scenario = machine.getChildren().get(0);
		Assert.assertEquals(SCENARIO_NAME, scenario.getName());
		final TestNode test = (TestNode) scenario.getChildren().get(0);
		Assert.assertEquals(TEST_NAME, test.getName());
		Assert.assertEquals(uid, test.getUid());
		TestDetails testDetails = getTestDetails(uid);
		Assert.assertNotNull(testDetails);
		Assert.assertEquals(TEST_DETAILS_NAME, testDetails.getName());
		return testDetails;
	}

}
