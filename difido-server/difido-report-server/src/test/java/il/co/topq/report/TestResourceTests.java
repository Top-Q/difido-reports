package il.co.topq.report;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.TestNode;

import org.junit.Test;

public class TestResourceTests extends AbstractResourceTestCase {

	@Test
	public void testAddTest() {
		String machineName = "Machine #1";
		String scenarioName = "Scenario #1";
		String testName = "Test #1";

		int executionId = addExecution();
		assertEquals(0, executionId);

		int machineId = addMachine(executionId, machineName);
		assertEquals(0, machineId);

		int scenarioId = addRootScenario(executionId, machineId, scenarioName);
		assertEquals(0, scenarioId);

		int testId = addTest(executionId, machineId, scenarioId, testName);
		assertEquals(0, testId);

		TestNode test = getTest(executionId, machineId, scenarioId, testId);
		assertEquals(testName, test.getName());

	}

}
