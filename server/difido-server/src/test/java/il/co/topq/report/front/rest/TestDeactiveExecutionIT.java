package il.co.topq.report.front.rest;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class TestDeactiveExecutionIT extends AbstractResourceTest {

	private static final int NUM_OF_TESTS_IN_SCENARIO = 10;


	@Test
	public void testCreateAndDestroyExecution() throws Exception {
		final String executionUid = String.valueOf(System.currentTimeMillis() / 10000)
				+ String.valueOf(new Random().nextInt(100));
		final int executionId = client.addExecution(new ExecutionDetails("Testing", true));
		final MachineNode machine = new MachineNode("Machine");
		final int machineId = client.addMachine(executionId, machine);
		Assert.assertNotEquals("Recieved wrong maching id ", -1, machineId);
		final ScenarioNode scenario = new ScenarioNode("Scenario ");
		machine.addChild(scenario);
		for (int i = 0; i < NUM_OF_TESTS_IN_SCENARIO; i++) {
			final String uid = executionUid + "-" + i;
			final TestNode test = new TestNode(i, "Test " , uid);
			scenario.addChild(test);
			client.updateMachine(executionId, machineId, machine);
			TestDetails details = new TestDetails(uid);
			ReportElement element = new ReportElement(details);
			element.setType(ElementType.regular);
			element.setTime("00:" + i);
			element.setTitle("Element from thread #" + i);
			element.setMessage("Element message from thread #" + i);
			details.addReportElement(element);
			client.addTestDetails(executionId, details);
		}
		client.endExecution(executionId);
		//TODO: Assert that execution is no longer active
	}

}
