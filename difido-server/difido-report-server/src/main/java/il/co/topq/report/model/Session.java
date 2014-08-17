package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.TestNode;
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

	private List<Execution> executions;

	private boolean activeExecution = false;


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
	public void testDetailsAdded(TestDetails details) {
	}



}
