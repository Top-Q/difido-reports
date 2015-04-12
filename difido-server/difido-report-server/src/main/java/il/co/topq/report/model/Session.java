package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Session implements ResourceChangedListener {
	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(Session.class);

	//Package private for unit testing
	List<ExecutionMetaData> executions =  new ArrayList<ExecutionMetaData>();

	private volatile Object lockObject = new Object();

	public synchronized int addExecution() {
		Execution execution = new Execution();
		ExecutionMetaData metaData = new ExecutionMetaData(Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date()), execution);
		executions.add(metaData);
		final int executionId = executions.indexOf(metaData);
		ListenersManager.INSTANCE.notifyExecutionAdded(executionId, execution);
		return executionId;
	}

	public int getLastExecutionIndexAndAddIfNoneExist() {
		synchronized (lockObject) {			
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
		return executions.get(index).getExecution();
	}

	private boolean isExecutionActive(int index) {
		if (null == executions) {
			return false;
		}
		if (index >= executions.size()) {
			return false;
		}
		ExecutionMetaData metadata = executions.get(index);
		if (metadata == null || !metadata.isActive()) {
			return false;
		}
		final int maxIdleTime = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		final int idleTime = (int) (System.currentTimeMillis() - metadata.getLastAccessedTime()) / 1000;
		if (idleTime > maxIdleTime) {
			log.debug("Execution with id " + index + " idle time is " + idleTime
					+ " which exceeded the max idle time of " + maxIdleTime + ". Disabling execution");
			ListenersManager.INSTANCE.notifyExecutionEnded(index, executions.get(index).getExecution());
			return false;
		}
		metadata.setLastAccessedTime(System.currentTimeMillis());
		return true;

	}

	@Override
	public void executionAdded(int executionId, Execution execution) {
	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		final ExecutionMetaData metadata = executions.get(executionId);
		if (null == metadata) {
			log.error("Trying to disable execution with id " + executionId + " which is not exist");
		}
		metadata.setActive(false);
	}
	
	private void updateExecutionLastUpdateTime(int executionId){
		final ExecutionMetaData metadata = executions.get(executionId);
		if (null == metadata) {
			log.error("Trying to update machine in execution with id " + executionId + " which is not exist");
		}
		metadata.setLastAccessedTime(System.currentTimeMillis());
	}

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		updateExecutionLastUpdateTime(executionId);
	}

	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		updateExecutionLastUpdateTime(executionId);
	}
	
	class ExecutionMetaData {
		private boolean active;
		private long lastAccessedTime;
		private final Execution execution;
		private final String timestamp;
		
		public ExecutionMetaData(String timestamp,Execution execution){
			this.timestamp = timestamp;
			this.execution = execution;
			this.active = true;
			lastAccessedTime = System.currentTimeMillis();
		}
		
		public String getTimestamp() {
			return timestamp;
		}

		public Execution getExecution() {
			return execution;
		}

		public boolean isActive() {
			return active;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
		public long getLastAccessedTime() {
			return lastAccessedTime;
		}
		public void setLastAccessedTime(long lastAccessedTime) {
			this.lastAccessedTime = lastAccessedTime;
		}
		
		
	}

}
