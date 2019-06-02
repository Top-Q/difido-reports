package il.co.topq.report.front.rest;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class CreateMultipleExecutionsIT extends AbstractResourceTest {
	private static final String MACHINE_NAME = "My Machine";
	private static final String SCENARIO_NAME = "My Scenario";
	private static final String TEST_NAME = "My test";
	private static final int NUM_OF_EXECUTIONS = 10;

	private int executionId;
	private String uid;

	@Test
	@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
	public void testCreateSeparateExecutions() throws Exception {
		for (int i = 0; i < NUM_OF_EXECUTIONS; i++) {
			ExecutionDetails description = new ExecutionDetails();
			description.setShared(false);
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
			client.endExecution(executionId);
		}
		waitForTasksToFinish();
		final Execution[] executions = getAllExecutions();
		Assert.assertNotNull(executions);
		Assert.assertEquals(NUM_OF_EXECUTIONS, executions.length);
	}

	@Test
	@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
	public void testCreateSharedExecutions() throws Exception {
		ExecutionDetails description = new ExecutionDetails();
		description.setShared(true);
		executionId = client.addExecution(description);
		for (int i = 0; i < NUM_OF_EXECUTIONS; i++) {
			final MachineNode machine = new MachineNode(MACHINE_NAME + " " + i);
			final int machineId = client.addMachine(executionId, machine);
			final ScenarioNode scenario = new ScenarioNode(SCENARIO_NAME);
			machine.addChild(scenario);
			final TestNode test = new TestNode(0, TEST_NAME, "0");
			uid = String.valueOf(Math.abs(new Random().nextInt()));
			test.setUid(uid);
			scenario.addChild(test);
			client.updateMachine(executionId, machineId, machine);
		}
		client.endExecution(executionId);
		waitForTasksToFinish();
		final Execution[] executions = getAllExecutions();
		Assert.assertNotNull(executions);
		Assert.assertEquals(1, executions.length);
		Assert.assertNotNull(executions[0].getMachines());
		Assert.assertEquals(NUM_OF_EXECUTIONS, executions[0].getMachines().size());
		for (int i = 0 ; i < NUM_OF_EXECUTIONS ; i++) {
			Assert.assertEquals(MACHINE_NAME + " " + i, executions[0].getMachines().get(i).getName());
		}
	}
}
