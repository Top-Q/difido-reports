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

	private boolean activeExecution = false;

	private AbstractMap<Integer, TestDetails> testDetails = new ConcurrentHashMap<Integer, TestDetails>(
			new HashMap<Integer, TestDetails>());

	public void addTestDetails(TestNode test, TestDetails details) {
		testDetails.put(test.getIndex(), details);
	}

	public synchronized TestDetails getTestDetails(TestNode test) {
		return testDetails.get(test.getIndex());
	}

	public synchronized int addExecution() {
		Execution execution = new Execution();
		createExecutionListIfNull();
		executions.add(execution);
		activeExecution = true;
		return executions.indexOf(execution);
	}

	/**
	 * Get the last active execution.
	 * 
	 * @return last execution or null if none exists
	 */
	public synchronized Execution getLastActiveExecution() {
		createExecutionListIfNull();
		if (!activeExecution) {
			return null;
		}
		if (executions.isEmpty()) {
			return null;
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
		if (null == executions) {
			executions = Collections.synchronizedList(new ArrayList<Execution>());
		}
	}

	public List<Execution> getExecutions() {
		createExecutionListIfNull();
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
		activeExecution = false;
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
		testDetails.remove(test.getIndex());

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
