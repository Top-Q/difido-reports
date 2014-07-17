package il.co.topq.report.controller.resource;

import il.co.topq.difido.client.DifidoClient;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Main;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class EndToEndTests extends AbstractResourceTestCase {

	private static final int NUM_OF_MACHINES = 2;
	private static final int NUM_OF_SCENARIOS_IN_MACHINE = 2;
	private static final int NUM_OF_TESTS_IN_SCENARIO = 2;
	private static final int NUM_OF_REPORT_ELEMENTS_IN_TEST = 4;
	private static final int NUM_OF_ITERATIONS = 10;

	private DifidoClient client;

	@Before
	public void before() {
		client = DifidoClient.build(Main.BASE_URI);
	}

	@Test
	public void createSimpleReport() {
		for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
			createReport();

		}
	}

	@Test
	public void testReportElementAddingTime() {
		int numOfReportElements = 100;
		int executionId = client.addExecution();
		int machineId = client.addMachine(executionId, new MachineNode("machine"));
		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode("root_scenario"));
		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode("test") );
		TestDetails details = new TestDetails();
		details.setName("test");
		details.setDuration((int) (Math.random() * 1000));
		details.setTimeStamp(new Date().toString());
		details.setDescription("Some random number as description: " + System.currentTimeMillis());
		client.addTestDetails(executionId, machineId, scenarioId, testId, details);
		for (int elementIndex = 0; elementIndex < numOfReportElements; elementIndex++) {
			ReportElement element = new ReportElement();
			element.setTitle("My report element");
			element.setTime(elementIndex+"");
			element.setStatus(elementIndex % 2 == 0 ? Status.success : Status.failure);
			long start = System.currentTimeMillis();
			client.addReportElement(executionId, machineId, scenarioId, testId, element);
			System.out.println("Element added in " + (System.currentTimeMillis() - start) + " mills");
		}

	}

	private void createReport() {
		long start = System.currentTimeMillis();
		int executionId = client.addExecution();
		for (int machineIndex = 0; machineIndex < NUM_OF_MACHINES; machineIndex++) {
			int machineId = client.addMachine(executionId, new MachineNode("machine" + machineIndex));
			int scenarioId = -1;
			for (int scenarioIndex = 0; scenarioIndex < NUM_OF_SCENARIOS_IN_MACHINE; scenarioIndex++) {
				if (scenarioId == -1) {
					scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode("root_scenario" + scenarioIndex));
				} else {
					scenarioId = client.addSubScenario(executionId, machineId, scenarioId, new ScenarioNode("sub_scenario" + scenarioId));
				}
				for (int testIndex = 0; testIndex < NUM_OF_TESTS_IN_SCENARIO; testIndex++) {
					int testId = client.addTest(executionId, machineId, scenarioId, new TestNode("test" + testIndex));
					TestDetails details = new TestDetails();
					details.setName("test" + testIndex);
					details.setDuration((int) (Math.random() * 1000));
					details.setTimeStamp(new Date().toString());
					details.setDescription("Some random number as description: " + System.currentTimeMillis());
					client.addTestDetails(executionId, machineId, scenarioId, testId, details);
					for (int elementIndex = 0; elementIndex < NUM_OF_REPORT_ELEMENTS_IN_TEST; elementIndex++) {
						ReportElement element = new ReportElement();
						element.setTitle("My report element");
						element.setTime("10");
						element.setStatus(elementIndex % 2 == 0 ? Status.success : Status.failure);
						client.addReportElement(executionId, machineId, scenarioId, testId, element);
					}

				}

			}

		}
		client.endExecution(executionId);

		System.out.println("Report generation finished in " + (System.currentTimeMillis() - start) + " mills");
	}
}
