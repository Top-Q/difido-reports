package il.co.topq.report.listener;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.resource.AbstractResourceTestCase;

public class ResourceAddedListenerTests extends AbstractResourceTestCase implements ResourceAddedListener {

	private Execution notifiedExecution;
	private MachineNode notifiedMachine;
	private ScenarioNode notifiedScenario;
	private TestNode notifiedTest;
	private TestDetails notifiedDetails;
	private ReportElement notifiedElement;

	@Before
	public void classSetup() {
		ListenersManager.INSTANCE.addListener(this);
	}

	@After
	public void classTearDown() {
		ListenersManager.INSTANCE.removeListener(this);

	}

	@Test
	public void testAllEventsAreBroadcasted() {
		String machineName = "Machine #1";
		String rootScenarioName = "Scenario #1";
		String subScenarioName = "Sub Scenario #1";
		String testName = "Test #1";

		int executionId = addExecution();
		Assert.assertNotNull(notifiedExecution);

		int machineId = addMachine(executionId, machineName);
		assertEquals(machineName, notifiedMachine.getName());

		int scenarioId = addRootScenario(executionId, machineId, rootScenarioName);
		assertEquals(rootScenarioName, notifiedScenario.getName());

		scenarioId = addSubScenario(executionId, machineId, scenarioId, subScenarioName);
		assertEquals(subScenarioName, notifiedScenario.getName());

		int testId = addTest(executionId, machineId, scenarioId, testName);
		assertEquals(testName, notifiedTest.getName());

		TestDetails details = new TestDetails();
		details.setName(testName);
		addTestDetails(executionId, machineId, scenarioId, testId, details);
		assertEquals(details.getName(), notifiedDetails.getName());

		ReportElement element = new ReportElement();
		element.setTitle("My title");
		element.setMessage("My message");
		element.setStatus(Status.success);
		element.setType(ElementType.regular);
		addReportElement(executionId, machineId, scenarioId, testId, element);
		assertEquals(element.getTitle(), notifiedElement.getTitle());
	}

	@Override
	public void executionAdded(Execution execution) {
		this.notifiedExecution = execution;
	}

	@Override
	public void machineAdded(MachineNode machine) {
		notifiedMachine = machine;

	}

	@Override
	public void scenarioAdded(ScenarioNode scenario) {
		notifiedScenario = scenario;

	}

	@Override
	public void testAdded(TestNode test) {
		notifiedTest = test;
	}

	@Override
	public void reportElementAdded(ReportElement element) {
		notifiedElement = element;
	}

	@Override
	public void testDetailsAdded(TestDetails details) {
		notifiedDetails = details;

	}
}
