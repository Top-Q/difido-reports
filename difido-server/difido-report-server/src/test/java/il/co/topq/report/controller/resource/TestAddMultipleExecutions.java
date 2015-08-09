package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.model.AbstractResourceTestCase;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class TestAddMultipleExecutions extends AbstractResourceTestCase {
	private static final String MACHINE_NAME = "My Machine";
	private static final String SCENARIO_NAME = "My Scenario";
	private static final String TEST_NAME = "My test";
	private static final int NUM_OF_EXECUTIONS = 10;

	
	private DifidoClient client;
	private String host = "localhost";
	private int port = 8080;
	private int executionId;
	private String uid;

	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		client = new DifidoClient(host, port);
	}
	
	@Test
	public void testMergeMachine() throws Exception {
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
		
		final TestNode test2 = new TestNode(0, TEST_NAME + 2, "1");
		uid = String.valueOf(Math.abs(new Random().nextInt()));
		test.setUid(uid);
		scenario.addChild(test2);
		
		client.updateMachine(executionId, machineId, machine);
		
		
	}
	
	@Test
	public void testAddConcurrentExecutions() throws Exception{
		for (int i = 0 ; i < NUM_OF_EXECUTIONS ; i++){
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
		}
		System.out.println("End");
	}
}
