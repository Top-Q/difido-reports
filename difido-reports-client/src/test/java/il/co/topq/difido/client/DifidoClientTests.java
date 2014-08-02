package il.co.topq.difido.client;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import org.junit.Ignore;
import org.junit.Test;

public class DifidoClientTests extends AbstractTestCase {

	@Test
	@Ignore
	public void testAddExecution() {
		int executionId = client.addExecution();
		assertEquals(0, executionId);
		client.endExecution(executionId);
		executionId = client.addExecution();
		assertEquals(1, executionId);
		client.endExecution(executionId);

	}

	@Test
	@Ignore
	public void testAddDetails() {
		String machineName = "Machine #1";
		String scenarioName = "Scenario #1";
		String testName = "Test #1";

		int executionId = client.addExecution();
		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(scenarioName));
		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode(testName));
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
	@Ignore
	public void testReportElementAddingTime() {
		int numOfReportElements = 100;
		int executionId = client.addExecution();
		int machineId = client.addMachine(executionId, new MachineNode("machine"));
		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode("root_scenario"));
		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode("test"));
		TestDetails details = new TestDetails();
		details.setName("test");
		details.setDuration((int) (Math.random() * 1000));
		details.setTimeStamp(new Date().toString());
		details.setDescription("Some random number as description: " + System.currentTimeMillis());
		client.addTestDetails(executionId, machineId, scenarioId, testId, details);
		for (int elementIndex = 0; elementIndex < numOfReportElements; elementIndex++) {
			ReportElement element = new ReportElement();
			element.setTitle("My report element");
			element.setTime(elementIndex + "");
			element.setStatus(elementIndex % 2 == 0 ? Status.success : Status.failure);
			long start = System.currentTimeMillis();
			client.addReportElement(executionId, machineId, scenarioId, testId, element);
			System.out.println("Element added in " + (System.currentTimeMillis() - start) + " mills");
		}
	}

}
