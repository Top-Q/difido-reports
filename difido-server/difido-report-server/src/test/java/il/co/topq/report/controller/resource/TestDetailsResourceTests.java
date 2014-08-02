package il.co.topq.report.controller.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.model.Session;
import il.co.topq.report.view.HtmlViewGenerator;

import org.junit.Test;

public class TestDetailsResourceTests extends AbstractResourceTestCase {

	@Test
	public void testAddDetails() {
		String machineName = "Machine #1";
		String scenarioName = "Scenario #1";
		String testName = "Test #1";

		int executionId = client.addExecution();
		assertEquals(0, executionId);

		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		assertEquals(0, machineId);

		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(scenarioName));
		assertEquals(0, scenarioId);

		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode(testName));
		assertEquals(0, testId);

		String description = "Test description";
		long duration = 6000;
		String name = "Test name";
		String timestamp = "timestamp";
		TestDetails details = new TestDetails();
		details.addParameter("param1", "val1");
		details.addProperty("prop1", "val1");
		details.setDescription(description);
		details.setDuration(duration);
		details.setName(name);
		details.setTimeStamp(timestamp);
		client.addTestDetails(executionId, machineId, scenarioId, testId, details);
		TestDetails actualDetails = client.getTestDetails(executionId, machineId, scenarioId, testId);
		assertEquals(name, actualDetails.getName());
		assertEquals(timestamp, actualDetails.getTimeStamp());
		assertEquals(description, actualDetails.getDescription());

	}

	@Test
	public void testAddReportElement() {
		String machineName = "Machine #1";
		String scenarioName = "Scenario #1";
		String testName = "Test #1";

		int executionId = client.addExecution();
		assertEquals(0, executionId);

		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		assertEquals(0, machineId);

		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(scenarioName));
		assertEquals(0, scenarioId);

		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode(testName));
		assertEquals(0, testId);

		String description = "Test description";
		long duration = 6000;
		String name = "Test name";
		String timestamp = "timestamp";
		TestDetails details = new TestDetails();
		details.addParameter("param1", "val1");
		details.addProperty("prop1", "val1");
		details.setDescription(description);
		details.setDuration(duration);
		details.setName(name);
		details.setTimeStamp(timestamp);
		client.addTestDetails(executionId, machineId, scenarioId, testId, details);
		ReportElement element = new ReportElement();
		element.setTitle("My title");
		element.setMessage("My message");
		element.setStatus(Status.success);
		element.setType(ElementType.regular);
		client.addReportElement(executionId, machineId, scenarioId, testId, element);
		ReportElement[] elements = getReportElements(executionId, machineId, scenarioId, testId);
		System.out.println(elements[0]);
		assertEquals(element.getTitle(), elements[0].getTitle());
		assertEquals(element.getMessage(), elements[0].getMessage());
		assertEquals(element.getTime(), elements[0].getTime());
		assertEquals(element.getStatus(), elements[0].getStatus());
		assertEquals(element.getType(), elements[0].getType());

	}

	@Test
	public void testAddFile() {
		
		String currentDir = System.getProperty("user.dir");
		String uploadedFilePath = currentDir + File.separator +
				"src" + File.separator +
				"test" + File.separator +
				"resources" + File.separator + "top-q.pdf";
		
		String machineName = "Machine #1";
		String scenarioName = "Scenario with attached file";
		String testName = "Test with attached file";

		int executionId = client.addExecution();
		assertEquals(0, executionId);

		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		assertEquals(0, machineId);

		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(scenarioName));
		assertEquals(0, scenarioId);

		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode(testName));
		assertEquals(0, testId);

		String description = "Test description";
		long duration = 6000;
		String name = "Test name";
		String timestamp = "timestamp";
		TestDetails details = new TestDetails();
		details.addParameter("param1", "val1");
		details.addProperty("prop1", "val1");
		details.setDescription(description);
		details.setDuration(duration);
		details.setName(name);
		details.setTimeStamp(timestamp);
		client.addTestDetails(executionId, machineId, scenarioId, testId, details);
		
		File uploadedFile = new File(uploadedFilePath);
		client.addFile(executionId, machineId, scenarioId, testId, uploadedFile);
		
		ReportElement element = new ReportElement();
		element.setType(ElementType.lnk);
		element.setTitle("Attached file: " + uploadedFile.getName());
		
		ReportElement[] elements = getReportElements(executionId, machineId, scenarioId, testId);
		assertEquals(element.getTitle(), elements[0].getTitle());
		assertEquals(element.getType(), elements[0].getType());
		
		File executionDestinationFolder = HtmlViewGenerator.getInstance().getExecutionDestinationFolder();
		
		String fileOnServerPath = executionDestinationFolder + File.separator +
				"tests" + File.separator +
				"test_" + Session.INSTANCE.getTestIndex() + File.separator + uploadedFile.getName();
		
		assertTrue(new File(fileOnServerPath).exists());
	}
	
	private ReportElement[] getReportElements(int executionId, int machineId, int scenarioId, int testId) {
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		final TestNode test = (TestNode) node;
		TestDetails details = Session.INSTANCE.getTestDetails(test);
		final ReportElement[] elements = details.getReportElements().toArray(
				new ReportElement[details.getReportElements().size()]);
		return elements;
	}

}
