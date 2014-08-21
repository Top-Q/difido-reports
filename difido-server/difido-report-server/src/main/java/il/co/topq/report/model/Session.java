package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Session implements ResourceChangedListener {
	INSTANCE;

	private volatile List<Execution> executions;

	private volatile boolean activeExecution = false;

	private volatile Object lockObject = new Object();

	public synchronized int addExecution() {
		Execution execution = new Execution();
		createExecutionListIfNull();
		executions.add(execution);
		ListenersManager.INSTANCE.notifyExecutionAdded(execution);
		activeExecution = true;
		return executions.indexOf(execution);
	}

	/**
	 * Get the last active execution.
	 * 
	 * @return last execution or null if none exists
	 */
	public Execution getLastActiveExecution() {
		synchronized (lockObject) {
			createExecutionListIfNull();
			if (!activeExecution) {
				return null;
			}
			if (executions.isEmpty()) {
				return null;
			}
			return getExecution(executions.size() - 1);
		}
	}
	

	public int getLastExecutionIndexAndAddIfNoneExist() {
		synchronized (lockObject) {
			createExecutionListIfNull();
			if (!activeExecution) {
				addExecution();
			}
			if (executions.isEmpty()) {
				addExecution();
			}
			return executions.size() - 1;
		}

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
