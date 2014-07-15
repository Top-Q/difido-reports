package il.co.topq.report.resource;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.util.Date;

import org.junit.Test;

public class EndToEndTests extends AbstractResourceTestCase {

	private static final int NUM_OF_MACHINES = 2;
	private static final int NUM_OF_SCENARIOS_IN_MACHINE = 3;
	private static final int NUM_OF_TESTS_IN_SCENARIO = 6;
	private static final int NUM_OF_REPORT_ELEMENTS_IN_TEST = 4;

	@Test
	public void createSimpleReport() {
		long start = System.currentTimeMillis();
		int executionId = addExecution();
		for (int machineIndex = 0; machineIndex < NUM_OF_MACHINES; machineIndex++) {
			int machineId = addMachine(executionId, "machine" + machineIndex);
			int scenarioId = -1;
			for (int scenarioIndex = 0; scenarioIndex < NUM_OF_SCENARIOS_IN_MACHINE; scenarioIndex++) {
				if (scenarioId == -1) {
					scenarioId = addRootScenario(executionId, machineId, "root_scenario" + scenarioIndex);
				} else {
					scenarioId = addSubScenario(executionId, machineId, scenarioId, "sub_scenario" + scenarioId);
				}
				for (int testIndex = 0; testIndex < NUM_OF_TESTS_IN_SCENARIO; testIndex++) {
					int testId = addTest(executionId, machineId, scenarioId, "test" + testIndex);
					TestDetails details = new TestDetails();
					details.setTimeStamp(new Date().toString());
					addTestDetails(executionId, machineId, scenarioId, testId, details);
					for (int elementIndex = 0; elementIndex < NUM_OF_REPORT_ELEMENTS_IN_TEST; elementIndex++) {
						ReportElement element = new ReportElement();
						element.setTitle("My report element");
						element.setTime("10");
						element.setStatus(elementIndex % 2 == 0 ? Status.success : Status.failure);
						addReportElement(executionId, machineId, scenarioId, testId, element);
					}

				}

			}

		}
		System.out.println("Test finished in " + (System.currentTimeMillis() - start) + " mills");
	}
}
