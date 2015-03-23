package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Session implements ResourceChangedListener {
	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(Session.class);

	private volatile List<Execution> executions;

	private volatile List<ExecutionReport> executionReports;

	private volatile Object lockObject = new Object();

	public synchronized int addExecution() {
		Execution execution = new Execution();
		createExecutionListIfNull();
		executions.add(execution);
		ExecutionReport executionReport = new ExecutionReport();
		executionReports.add(executionReport);
		final int executionId = executions.indexOf(execution);
		ListenersManager.INSTANCE.notifyExecutionAdded(executionId, execution);
		return executionId;
	}

	public int getLastExecutionIndexAndAddIfNoneExist() {
		synchronized (lockObject) {
			createExecutionListIfNull();
			if (executions.isEmpty()) {
				return addExecution();
			}
			final int lastExecutionIndex = executions.size() - 1;
			if (isExecutionActive(lastExecutionIndex)) {
				return lastExecutionIndex;
			}
			return addExecution();
		}

	}

	public Execution getExecution(int index) {
		return executions.get(index);
	}

	private boolean isExecutionActive(int index) {
		if (null == executions) {
			return false;
		}
		if (index >= executions.size()) {
			return false;
		}
		ExecutionReport executionReport = executionReports.get(index);
		if (executionReport == null || !executionReport.isActive()) {
			return false;
		}
		final int maxIdleTime = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		final int idleTime = (int) (System.currentTimeMillis() - executionReport.getLastAccessedTime()) / 1000;
		if (idleTime > maxIdleTime) {
			log.debug("Execution with id " + index + " idle time is " + idleTime
					+ " which exceeded the max idle time of " + maxIdleTime + ". Disabling execution");
			ListenersManager.INSTANCE.notifyExecutionEnded(index, executions.get(index));
			return false;
		}
		executionReport.setLastAccessedTime(System.currentTimeMillis());
		return true;

	}

	private void createExecutionListIfNull() {
		if (null == executions) {
			executions = Collections.synchronizedList(new ArrayList<Execution>());
		}
		if (null == executionReports) {
			executionReports = Collections.synchronizedList(new ArrayList<ExecutionReport>());
		}
	}

	public List<Execution> getExecutions() {
		createExecutionListIfNull();
		return executions;
	}

	public synchronized void flush() {
		executions = null;
		executionReports = null;
	}

	@Override
	public void executionAdded(int executionId, Execution execution) {
	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		final ExecutionReport executionReport = executionReports.get(executionId);
		if (null == executionReport) {
			log.error("Trying to disable execution with id " + executionId + " which is not exist");
		}
		executionReport.setActive(false);
	}

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		final ExecutionReport executionReport = executionReports.get(executionId);
		if (null == executionReport) {
			log.error("Trying to update machine in execution with id " + executionId + " which is not exist");
		}
		executionReport.setLastAccessedTime(System.currentTimeMillis());
	}

	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		final ExecutionReport executionReport = executionReports.get(executionId);
		if (null == executionReport) {
			log.error("Trying to update details in execution with id " + executionId + " which is not exist");
		}
		executionReport.setLastAccessedTime(System.currentTimeMillis());
	}

}
