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

	
	private int executionId;
	private String uid;

	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
	}
	
	@Test
	public void testAddConcurrentExecutions() throws Exception{
		for (int i = 0 ; i < NUM_OF_EXECUTIONS ; i++){
			ExecutionDetails description = new ExecutionDetails();
			description.setShared(false);
			executionId = difidoClient.addExecution(description);
			final MachineNode machine = new MachineNode(MACHINE_NAME);
			final int machineId = difidoClient.addMachine(executionId, machine);
			final ScenarioNode scenario = new ScenarioNode(SCENARIO_NAME);
			machine.addChild(scenario);
			final TestNode test = new TestNode(0, TEST_NAME, "0");
			uid = String.valueOf(Math.abs(new Random().nextInt()));
			test.setUid(uid);
			scenario.addChild(test);
			difidoClient.updateMachine(executionId, machineId, machine);
		}
		System.out.println("End");
	}
}
