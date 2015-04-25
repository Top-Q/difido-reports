package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.elasticsearch.ESUtils;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum ExecutionManager implements ResourceChangedListener {
	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(ExecutionManager.class);

	// Package private for unit testing
	Map<Integer, ExecutionMetaData> executionsCache = new HashMap<Integer, ExecutionMetaData>();

	private volatile Object lockObject = new Object();

	public int addExecution() {
//		synchronized (lockObject) {
			Execution execution = new Execution();
			final Date executionDate = new Date();
			final ExecutionMetaData metaData = new ExecutionMetaData(
					Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(executionDate), execution);
			metaData.setTime(Common.API_TIME_FORMATTER.format(executionDate));
			metaData.setDate(Common.API_DATE_FORMATTER.format(executionDate));
			
			int maxId = 0;
			if (ESUtils.isExistType(Common.DIFIDO_INDEX, "meta")){
				maxId = (int) ESUtils.max(Common.DIFIDO_INDEX, "meta", "id");
				if (maxId < 0) {
					maxId = -1;
				}
			}
			metaData.setId(maxId + 1);
			metaData.setFolderName(Common.EXECUTION_REPORT_FOLDER_PREFIX + "_"
					+ Common.EXECUTION_REPROT_TIMESTAMP_FORMATTER.format(new Date()));
			metaData.setUri(Common.REPORTS_FOLDER_NAME + "/" + metaData.getFolderName() + "/index.html");
			metaData.setActive(true);
			executionsCache.put(metaData.getId(), metaData);
			try {
				ESUtils.safeAdd(Common.DIFIDO_INDEX, "meta", String.valueOf(metaData.getId()), metaData);
			} catch (Exception e) {
				log.warn("Failed adding execution metadata Elastiseach", e);
			}
			ListenersManager.INSTANCE.notifyExecutionAdded(metaData.getId(), execution);
			return metaData.getId();
//		}
	}

	public ExecutionMetaData getExecutionMetaData(int executionId) {
		ExecutionMetaData executionMetaData = executionsCache.get(executionId);
		if (null == executionMetaData) {
			try {
				executionMetaData = ESUtils.get(Common.DIFIDO_INDEX, "meta", String.valueOf(executionId),
						ExecutionMetaData.class);
			} catch (Exception e) {
				log.warn("Execution meta data for execution with id " + executionId
						+ " is not exists in the Difido server");
			}
		}
		return new ExecutionMetaData(executionMetaData);
	}

	public ExecutionMetaData[] getAllMetaData() {
		try {
			return ESUtils.getAll(Common.DIFIDO_INDEX, "meta", ExecutionMetaData.class).toArray(
					new ExecutionMetaData[] {});
		} catch (Exception e) {
			log.error("Failed getting all the execution metadatas from the Elasticsearch", e);
		}
		return new ExecutionMetaData[] {};
	}

	public int getLastExecutionIndexAndAddIfNoneExist() {
		synchronized (lockObject) {
			if (executionsCache.isEmpty()) {
				return addExecution();
			}
			for (int executionIndex : executionsCache.keySet()){
				if (isExecutionActive(executionIndex)){
					return executionIndex;
				}
			}
			return addExecution();
		}

	}

	public Execution getExecution(int index) {
		return executionsCache.get(index).getExecution();
	}

	private boolean isExecutionActive(int index) {
		if (null == executionsCache) {
			return false;
		}
		if (index >= executionsCache.size()) {
			return false;
		}
		ExecutionMetaData metadata = executionsCache.get(index);
		if (metadata == null || !metadata.isActive()) {
			return false;
		}
		final int maxIdleTime = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		final int idleTime = (int) (System.currentTimeMillis() - metadata.getLastAccessedTime()) / 1000;
		if (idleTime > maxIdleTime) {
			log.debug("Execution with id " + index + " idle time is " + idleTime
					+ " which exceeded the max idle time of " + maxIdleTime + ". Disabling execution");
			ListenersManager.INSTANCE.notifyExecutionEnded(index, executionsCache.get(index).getExecution());
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
		final ExecutionMetaData metadata = executionsCache.get(executionId);
		if (null == metadata) {
			log.error("Trying to disable execution with id " + executionId + " which is not exist");
		}
		metadata.setActive(false);
		try {
			ESUtils.update(Common.DIFIDO_INDEX, "meta", String.valueOf(executionId), metadata);
		} catch (Exception e) {
			log.error("Failed updating execution metadata with id " + executionId + " in the Elasticsearch");
		}
	}

	private void updateExecutionLastUpdateTime(int executionId) {
		final ExecutionMetaData metadata = executionsCache.get(executionId);
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

	public static class ExecutionMetaData {
		/**
		 * The id of the execution
		 */
		private int id;

		/**
		 * The name of the folder in the file system that holds the report file.<br>
		 * e.g. execution_2015_04_14__22_15_38_84 <br>
		 */
		private String folderName;

		/**
		 * The uri of the index file of the execution report. <br>
		 * e.g. reports/execution_2015_04_14__22_15_38_84/index.html
		 */
		private String uri;

		/**
		 * The date in which the execution was created in. <br>
		 * e.g. 16/10/2016
		 */
		private String date;

		/**
		 * The time in which the execution was created in. <br>
		 * 12:32:11:23
		 */
		private String time;

		private boolean active;
		private long lastAccessedTime;

		@JsonIgnore
		private Execution execution;
		private String timestamp;

		public ExecutionMetaData() {

		}

		public ExecutionMetaData(final ExecutionMetaData metaData) {
			if (null != metaData) {
				this.active = metaData.active;
				this.date = metaData.date;
				this.execution = metaData.execution;
				this.folderName = metaData.folderName;
				this.id = metaData.id;
				this.lastAccessedTime = metaData.lastAccessedTime;
				this.time = metaData.time;
				this.timestamp = metaData.timestamp;
				this.uri = metaData.uri;
			}
		}

		public ExecutionMetaData(String timestamp, Execution execution) {
			this.timestamp = timestamp;
			this.execution = execution;
			this.active = true;
			lastAccessedTime = System.currentTimeMillis();
		}

		public String getTimestamp() {
			return timestamp;
		}

		@JsonIgnore
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

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getFolderName() {
			return folderName;
		}

		public void setFolderName(String folderName) {
			this.folderName = folderName;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		@JsonIgnore
		public void setExecution(Execution execution) {
			this.execution = execution;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

	}

}
