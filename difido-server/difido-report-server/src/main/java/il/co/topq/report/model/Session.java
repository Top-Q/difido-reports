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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public enum Session implements ResourceChangedListener {
	INSTANCE;

	private static AtomicInteger testIndex = new AtomicInteger(0);

	private List<Execution> executions;

	private AbstractMap<TestNode, TestDetails> detailsForTests = new ConcurrentHashMap<TestNode, TestDetails>(
			new HashMap<TestNode, TestDetails>());

	public synchronized void addTestDetails(TestNode test, TestDetails details) {
		detailsForTests.put(test, details);
	}

	public synchronized TestDetails getTestDetails(TestNode test) {
		return detailsForTests.get(test);
	}

	public synchronized int addExecution() {
		Execution execution = new Execution();
		createExecutionListIfNull();
		executions.add(execution);
		return executions.indexOf(execution);
	}

	/**
	 * Get the last execution. If there is not execution, an execution will be
	 * created
	 * 
	 * @return last execution
	 */
	public synchronized Execution getLastExecutionAndCreateIfNoneExist() {
		createExecutionListIfNull();
		if (executions.isEmpty()) {
			addExecution();
		}
		return getExecution(executions.size() - 1);
	}

	public synchronized Execution getExecution(int index) {
		if (null == executions) {
			return null;
		}
		if (index >= executions.size()) {
			// TODO: return error
		}
		return executions.get(index);
	}
	
	private void createExecutionListIfNull() {
		if (null == executions){
			executions = Collections.synchronizedList(new ArrayList<Execution>());
		}
	}

	public List<Execution> getExecutions() {
		return executions;
	}

	public synchronized void flush() {
		executions = null;
	}

	public int incrementAndGetTestIndex() {
		return testIndex.incrementAndGet();

	}

	@Override
	public void executionAdded(Execution execution) {
	}

	@Override
	public void executionEnded(Execution execution) {
	}

	@Override
	public void machineAdded(MachineNode machine) {
	}

	@Override
	public void scenarioAdded(ScenarioNode scenario) {
	}

	@Override
	public void testAdded(TestNode test) {
	}

	@Override
	public void testEnded(TestNode test) {
		detailsForTests.remove(test);

	}

	@Override
	public void testDetailsAdded(TestNode test, TestDetails details) {
	}

	@Override
	public void reportElementAdded(TestNode test, ReportElement element) {
	}
	
	public int getTestIndex() {
		return testIndex.intValue();
	}

}
