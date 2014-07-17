package il.co.topq.report.controller.listener;

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
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.controller.resource.AbstractResourceTestCase;

public class ResourceAddedListenerTests extends AbstractResourceTestCase implements ResourceChangedListener {

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

		int executionId = client.addExecution();
		Assert.assertNotNull(notifiedExecution);

		int machineId = client.addMachine(executionId, new MachineNode(machineName));
		assertEquals(machineName, notifiedMachine.getName());

		int scenarioId = client.addRootScenario(executionId, machineId, new ScenarioNode(rootScenarioName));
		assertEquals(rootScenarioName, notifiedScenario.getName());

		scenarioId = client.addSubScenario(executionId, machineId, scenarioId, new ScenarioNode(subScenarioName));
		assertEquals(subScenarioName, notifiedScenario.getName());

		int testId = client.addTest(executionId, machineId, scenarioId, new TestNode(testName));
		assertEquals(testName, notifiedTest.getName());

		TestDetails details = new TestDetails();
		details.setName(testName);
		client.addTestDetails(executionId, machineId, scenarioId, testId, details);
		assertEquals(details.getName(), notifiedDetails.getName());

		ReportElement element = new ReportElement();
		element.setTitle("My title");
		element.setMessage("My message");
		element.setStatus(Status.success);
		element.setType(ElementType.regular);
		client.addReportElement(executionId, machineId, scenarioId, testId, element);
		assertEquals(element.getTitle(), notifiedElement.getTitle());

		TestNode test = client.getTest(executionId, machineId, scenarioId, testId);
		test.setStatus(Status.success);
		client.updateTest(executionId, machineId, scenarioId, testId, test);
		Assert.assertNull(notifiedTest);
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
	public void reportElementAdded(TestNode test, ReportElement element) {
		notifiedElement = element;
	}

	@Override
	public void testDetailsAdded(TestNode test, TestDetails details) {
		notifiedDetails = details;

	}

	@Override
	public void executionEnded(Execution execution) {
	}

	@Override
	public void testEnded(TestNode test) {
		notifiedTest = null;
	}
}
