package il.co.topq.report.front.rest;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class ConcurrencyExecutionIT extends AbstractResourceTest {

	protected static final int NUM_OF_TESTS_IN_SCENARIO = 10;
	private static final int NUM_OF_THREADS = 10;

	@Test
	public void testConcurrentSharedExecutions() throws Exception {
		Thread[] threads = new Thread[NUM_OF_THREADS];
		long start = System.currentTimeMillis();
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threads[i] = new ExecutionRunThread(true, NUM_OF_TESTS_IN_SCENARIO);
			threads[i].start();
			// We need to give it a bit time to update the data. If not, we may
			// fail sometime because the server will not find a shared
			// execution.
			Thread.sleep(400);
		}
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threads[i].join();
		}

		System.out.println("Finished all reports in " + (System.currentTimeMillis() - start) + " millis");
		start = System.currentTimeMillis();

		final Execution execution = getExecution();
		Assert.assertNotNull(execution);
		Assert.assertEquals("Not all machines were joined to a single execution", NUM_OF_THREADS,
				execution.getMachines().size());
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

	@Test
	public void testConcurrentSeparateExecutions() throws Exception {
		Thread[] threads = new Thread[NUM_OF_THREADS];
		long start = System.currentTimeMillis();
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threads[i] = new ExecutionRunThread(false, NUM_OF_TESTS_IN_SCENARIO);
			threads[i].start();
		}
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threads[i].join();
		}

		System.out.println("Finished all reports in " + (System.currentTimeMillis() - start) + " millis");
		start = System.currentTimeMillis();

		final Execution[] executions = getAllExecutions();
		Assert.assertNotNull(executions);
		Assert.assertEquals(NUM_OF_THREADS, executions.length);
		for (int i = 0; i < executions.length; i++) {
			Assert.assertEquals(1, executions[i].getMachines().size());
			final MachineNode machine = executions[i].getMachines().get(0);
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

	class ExecutionRunThread extends Thread {

		private final boolean shared;
		private final int numOfTestsInScenario;

		ExecutionRunThread(boolean shared, int numOfTestsInScenario) {
			this.shared = shared;
			this.numOfTestsInScenario = numOfTestsInScenario;
		}

		@Override
		public void run() {
			try {
				final String tName = Thread.currentThread().getName();
				final String executionUid = String.valueOf(System.currentTimeMillis() / 10000)
						+ String.valueOf(new Random().nextInt(100));
				final ExecutionDetails execution = new ExecutionDetails("Testing", shared);
				final int executionId = client.addExecution(execution);
				System.out.println(tName + "- executionId " + executionId);
				final MachineNode machine = new MachineNode(tName);
				final int machineId = client.addMachine(executionId, machine);
				System.out.println(tName + "- machineId " + machineId);
				Assert.assertNotEquals("Recieved wrong maching id ", -1, machineId);
				final ScenarioNode scenario = new ScenarioNode("Scenario " + tName);
				machine.addChild(scenario);
				for (int i = 0; i < numOfTestsInScenario; i++) {
					final String uid = executionUid + "-" + i;
					final TestNode test = new TestNode(i, "Test " + tName, uid);
					scenario.addChild(test);
					client.updateMachine(executionId, machineId, machine);
					TestDetails details = new TestDetails(uid);
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

}
