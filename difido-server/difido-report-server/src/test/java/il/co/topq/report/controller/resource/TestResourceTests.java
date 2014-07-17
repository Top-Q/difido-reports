package il.co.topq.report.controller.resource;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.Enums.Status;

import org.junit.Test;

public class TestResourceTests extends AbstractResourceTestCase {

	@Test
	public void testAddTest() {
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

		TestNode test = client.getTest(executionId, machineId, scenarioId, testId);
		assertEquals(testName, test.getName());

	}

	@Test
	public void testUpdateTest() {
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

		TestNode test = client.getTest(executionId, machineId, scenarioId, testId);
		assertEquals(testName, test.getName());
		assertEquals(0, test.getDuration());
		assertEquals(null, test.getTimestamp());
		assertEquals(Status.success, test.getStatus());
		long duration = 5000;
		String timestamp = "timestamp";
		Status status = Status.warning;
		test.setDuration(duration);
		test.setTimestamp(timestamp);
		test.setStatus(status);
		client.updateTest(executionId, machineId, scenarioId, testId, test);
		test = null;
		test = client.getTest(executionId, machineId, scenarioId, testId);
		assertEquals(testName, test.getName());
		assertEquals(duration, test.getDuration());
		assertEquals(timestamp, test.getTimestamp());
		assertEquals(status, test.getStatus());

	}


}
