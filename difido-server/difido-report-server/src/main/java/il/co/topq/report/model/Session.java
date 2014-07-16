package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.listener.ResourceChangedListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public enum Session implements ResourceChangedListener{
	INSTANCE;

	private static AtomicInteger testIndex = new AtomicInteger(0);

	private List<Execution> executions;

	private AbstractMap<TestNode, TestDetails> detailsForTests = new ConcurrentHashMap<TestNode, TestDetails>(
			new HashMap<TestNode, TestDetails>());

	public void addTestDetails(TestNode test, TestDetails details) {
		detailsForTests.put(test, details);
	}
	
	public TestDetails getTestDetails(TestNode test) {
		return detailsForTests.get(test);
	}


	public int addExecution() {
		if (null == executions) {
			executions = new ArrayList<Execution>();
		}
		Execution execution = new Execution();
		executions.add(execution);
		return executions.indexOf(execution);
	}

	public Execution getExecution() {
		return getExecution(0);
	}

	public Execution getExecution(int index) {
		if (null == executions) {
			return null;
		}
		if (index >= executions.size()) {
			// TODO: return error
		}
		return executions.get(index);
	}

	public void flush() {
		executions = null;
	}

	public int incrementAndGetTestIndex() {
		return testIndex.incrementAndGet();

	}

	@Override
	public void executionAdded(Execution execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executionEnded(Execution execution) {
		// TODO Auto-generated method stub
		
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
		detailsForTests.remove(test);
		
	}

	@Override
	public void testDetailsAdded(TestNode test, TestDetails details) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportElementAdded(TestNode test, ReportElement element) {
		// TODO Auto-generated method stub
		
	}


}
