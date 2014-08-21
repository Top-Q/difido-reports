package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestConcurrencyExecutions extends AbstractResourceTestCase {

	protected static final int NUM_OF_TESTS_IN_SCENARIO = 20;
	private static final String host = "localhost";
	private static final int port = 8080;

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}
	
	class ExecutionRunThread extends Thread{
		
		@Override
		public void run() {
			try {
				final DifidoClient client = new DifidoClient(host, port);
				final String executionUid = String.valueOf(System.currentTimeMillis()/10000) + String.valueOf(new Random().nextInt(100));
				final int executionId = client.getLastExecutionId();
				System.out.println(Thread.currentThread().getName()+"- executionId "+executionId);
				final MachineNode machine = new MachineNode("My machine");
				final int machineId = client.addMachine(executionId, machine);
				System.out.println(Thread.currentThread().getName()+"- machineId "+machineId);
				final ScenarioNode scenario = new ScenarioNode("Scenario "+Thread.currentThread().getName());
				machine.addChild(scenario);
				for (int i = 0; i < NUM_OF_TESTS_IN_SCENARIO; i++) {
					final String uid = executionUid + "-" + i;
					final TestNode test = new TestNode(i, "My test", uid);
					scenario.addChild(test);
					client.updateMachine(executionId, machineId, machine);
					TestDetails details = new TestDetails("My Test Details", uid);
					ReportElement element = new ReportElement(details);
					element.setType(ElementType.regular);
					element.setTime("00:"+i);
					element.setTitle("My report element");
					element.setMessage("My report element message");
					details.addReportElement(element);
					client.addTestDetails(executionId, details);
				}
				
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
			
		}
	}

	@Test
	public void testJoinToExistingExecution() throws Exception {

		Thread t1 = new ExecutionRunThread();
		Thread t2 = new ExecutionRunThread();
		Thread t3 = new ExecutionRunThread();

		t1.start();
		Thread.sleep(500);
		t2.start();
		Thread.sleep(500);
		t3.start();
		t1.join();
		t2.join();
		t3.join();

	}

}
