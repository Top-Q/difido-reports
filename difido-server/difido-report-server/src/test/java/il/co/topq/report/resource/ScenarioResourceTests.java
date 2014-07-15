package il.co.topq.report.resource;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.ScenarioNode;

import org.junit.Test;

public class ScenarioResourceTests extends AbstractResourceTestCase {

	@Test
	public void testScenarioResource() {
		String machineName = "Machine #1";
		String scenarioName = "Scenario #1";

		int executionId = addExecution();
		assertEquals(0, executionId);

		int machineId = addMachine(executionId, machineName);
		assertEquals(0, machineId);

		int scenarioId = addRootScenario(executionId, machineId, scenarioName);
		assertEquals(0, scenarioId);

		ScenarioNode scenario = getScenario(executionId, machineId, scenarioId);
		assertEquals(scenarioName, scenario.getName());

	}

	@Test
	public void testAddSubScenarios() {
		String machineName = "Machine #1";
		String rootScenarioName = "Root scenrio";
		String subScenarioName = "Sub scenario";

		int executionId = addExecution();
		assertEquals(0, executionId);

		int machineId = addMachine(executionId, machineName);
		assertEquals(0, machineId);

		int rootScenarioId = addRootScenario(executionId, machineId, rootScenarioName);
		assertEquals(0, rootScenarioId);

		int subScenarioId = addSubScenario(executionId, machineId, rootScenarioId, subScenarioName);
		assertEquals(1, subScenarioId);

		ScenarioNode scenario = getScenario(executionId, machineId, rootScenarioId);
		assertEquals(rootScenarioName, scenario.getName());

		scenario = getScenario(executionId, machineId, subScenarioId);
		assertEquals(subScenarioName, scenario.getName());

		
	}

}
