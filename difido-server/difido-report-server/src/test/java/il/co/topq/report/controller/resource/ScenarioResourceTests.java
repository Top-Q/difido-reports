package il.co.topq.report.controller.resource;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;

import org.junit.Test;

public class ScenarioResourceTests extends AbstractResourceTestCase {

	@Test
	public void testScenarioResource() {
		String machineName = "Machine #1";
		String scenarioName = "Scenario #1";

		int executionId = client.addExecution();
		assertEquals(0, executionId);

		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		assertEquals(0, machineId);

		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(scenarioName));
		assertEquals(0, scenarioId);

		ScenarioNode scenario = client.getScenario(executionId, machineId, scenarioId);
		assertEquals(scenarioName, scenario.getName());

	}

	@Test
	public void testAddSubScenarios() {
		String machineName = "Machine #1";
		String rootScenarioName = "Root scenrio";
		String subScenarioName = "Sub scenario";

		int executionId = client.addExecution();
		assertEquals(0, executionId);

		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		assertEquals(0, machineId);

		int rootScenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(rootScenarioName));
		assertEquals(0, rootScenarioId);

		int subScenarioId = client.addSubScenario(executionId, machineId, rootScenarioId, new ScenarioNode(subScenarioName));
		assertEquals(1, subScenarioId);

		ScenarioNode scenario = client.getScenario(executionId, machineId, rootScenarioId);
		assertEquals(rootScenarioName, scenario.getName());

		scenario = client.getScenario(executionId, machineId, subScenarioId);
		assertEquals(subScenarioName, scenario.getName());

		
	}

}
