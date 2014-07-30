package il.co.topq.difido.client;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.TestDetails;

import org.junit.Ignore;
import org.junit.Test;

public class DifidoClientTests extends AbstractTestCase {
	
	@Test
	@Ignore
	public void testAddExecution(){
		int executionId = client.addExecution();
		assertEquals(0, executionId);
		client.endExecution(executionId);
		executionId = client.addExecution();
		assertEquals(1, executionId);
		client.endExecution(executionId);

	}
	
	@Test
//	@Ignore
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
}
