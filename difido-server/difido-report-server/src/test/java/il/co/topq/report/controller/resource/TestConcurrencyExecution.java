package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.model.AbstractResourceTestCase;

import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestConcurrencyExecution extends AbstractResourceTestCase {

	protected static final int NUM_OF_TESTS_IN_SCENARIO = 10;
	private static final int NUM_OF_THREADS = 10;
	private static final String host = "localhost";
	private static final int port = 8080;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		final DifidoClient client = new DifidoClient(host, port);
		client.deleteReports();
	}

	class ExecutionRunThread extends Thread {

		@Override
		public void run() {
			try {
				final String tName = Thread.currentThread().getName();
				final DifidoClient client = new DifidoClient(host, port);
				final String executionUid = String.valueOf(System.currentTimeMillis() / 10000)
						+ String.valueOf(new Random().nextInt(100));
				final int executionId = client.getLastExecutionId();
				System.out.println(tName + "- executionId " + executionId);
				final MachineNode machine = new MachineNode(tName);
				final int machineId = client.addMachine(executionId, machine);
				System.out.println(tName + "- machineId " + machineId);
				Assert.assertNotEquals("Recieved wrong maching id ", -1, machineId);
				final ScenarioNode scenario = new ScenarioNode("Scenario " + tName);
				machine.addChild(scenario);
				for (int i = 0; i < NUM_OF_TESTS_IN_SCENARIO; i++) {
					final String uid = executionUid + "-" + i;
					final TestNode test = new TestNode(i, "Test " + tName, uid);
					scenario.addChild(test);
					client.updateMachine(executionId, machineId, machine);
					TestDetails details = new TestDetails("Details " + tName + " #" + i, uid);
					ReportElement element = new ReportElement(details);
					element.setType(ElementType.regular);
					element.setTime("00:" + i);
					element.setTitle("Element from thread " + tName + " #" + i);
					element.setMessage("Element message from thread " + tName + " #" + i);
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
		Thread[] threads = new Thread[NUM_OF_THREADS];
		long start = System.currentTimeMillis();
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threads[i] = new ExecutionRunThread();
			threads[i].start();
		}
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threads[i].join();
		}

		System.out.println("Finished all reports in " + (System.currentTimeMillis() - start) + " millis");
		start = System.currentTimeMillis();
		
		final Execution execution = getExecution();
		Assert.assertNotNull(execution);
		Assert.assertEquals(NUM_OF_THREADS, execution.getMachines().size());
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			final MachineNode machine = execution.getMachines().get(0);
			final String threadName = machine.getName();
			Assert.assertEquals(1, machine.getChildren().size());
			final ScenarioNode scenario = machine.getChildren().get(0);
			Assert.assertTrue(scenario.getName().contains(threadName));
			Assert.assertEquals(NUM_OF_TESTS_IN_SCENARIO, scenario.getChildren().size());
			for (int j = 0; j < NUM_OF_TESTS_IN_SCENARIO; j++) {
				final String testName = scenario.getChildren().get(j).getName();
				Assert.assertTrue(testName.contains(threadName));
			}

		}
		System.out.println("Finished assertions in " + (System.currentTimeMillis() - start) + " millis");

	}

}
