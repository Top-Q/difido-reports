package il.co.topq.report.controller.listener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.resource.AbstractResourceTestCase;

public class ExecutionStatusListenerTests extends AbstractResourceTestCase implements ResourceChangedListener {

	private Execution notifiedStartedExecution;
	private Execution notifiedEndedExecution;

	@Before
	public void classSetUp() {
		ListenersManager.INSTANCE.addListener(this);
	}

	@Test
	public void testExecutionStartedAndEndedEvents() {
		int executionId = addExecution();
		Assert.assertNotNull(notifiedStartedExecution);
		endExecution(executionId);
		Assert.assertNotNull(notifiedEndedExecution);

	}

	@Override
	public void executionEnded(Execution execution) {
		this.notifiedEndedExecution = execution;
	}

	@Override
	public void executionAdded(Execution execution) {
		this.notifiedStartedExecution = execution;
		
	}

	@Override
	public void machineAdded(MachineNode machine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scenarioAdded(ScenarioNode scenario) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testAdded(TestNode test) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testEnded(TestNode test) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testDetailsAdded(TestNode test,TestDetails details) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportElementAdded(TestNode test,ReportElement element) {
		// TODO Auto-generated method stub
		
	}

}
