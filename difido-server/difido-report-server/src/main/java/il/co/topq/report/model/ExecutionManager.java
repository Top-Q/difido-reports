package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum ExecutionManager implements ResourceChangedListener {
	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(ExecutionManager.class);

	// Package private for unit testing
	Map<Integer, ExecutionMetaData> executionsCache;;

	private static Object lockObject = new Object();

	private static Object fileAccessLockObject = new Object();

	private static final String EXECUTION_FILE_NAME = "reports/meta.json";

	public int addExecution() {
		return addExecution(null);
	}

	/**
	 * Adding new execution. Execution can have multiple machines that can have
	 * scenarios with tests.
	 * 
	 * @param executionDetails
	 *            The description of the execution.
	 * @return The unique id of the newly created execution
	 */
	public int addExecution(ExecutionDetails executionDetails) {
		readExecutionMeta();
		Execution execution = new Execution();
		final Date executionDate = new Date();
		final ExecutionMetaData metaData = new ExecutionMetaData(
				Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(executionDate), execution);
		metaData.setTime(Common.API_TIME_FORMATTER.format(executionDate));
		metaData.setDate(Common.API_DATE_FORMATTER.format(executionDate));

		int maxId = getMaxId();
		metaData.setId(++maxId);
		metaData.setFolderName(Common.EXECUTION_REPORT_FOLDER_PREFIX + "_" + metaData.getId());
		metaData.setUri(Common.REPORTS_FOLDER_NAME + "/" + metaData.getFolderName() + "/index.html");
		metaData.setActive(true);
		if (executionDetails != null) {
			metaData.setDescription(executionDetails.getDescription());
			metaData.setShared(executionDetails.isShared());
			metaData.setProperties(executionDetails.getExecutionProperties());
		}
		executionsCache.put(metaData.getId(), metaData);
		writeExecutionMeta();
		ListenersManager.INSTANCE.notifyExecutionAdded(metaData.getId(), execution);
		return metaData.getId();
	}

	private void writeExecutionMeta() {
		if (null == executionsCache) {
			return;
		}
		synchronized (fileAccessLockObject) {
			final File executionMetaFile = getExecutionMetaFile();
			if (!executionMetaFile.exists()) {
				if (!executionMetaFile.getParentFile().exists()) {
					if (!executionMetaFile.getParentFile().mkdirs()) {
						log.error("Failed creating folder for execution meta file ");
						return;
					}
					try {
						if (!executionMetaFile.createNewFile()) {
							log.error("Failed creating execution meta file");
							return;
						}
					} catch (IOException e) {
						log.error("Failed creating execution meta file", e);
						return;
					}
				}
			}
			try {
				new ObjectMapper().writeValue(getExecutionMetaFile(), executionsCache);
			} catch (IOException e) {
				log.error("Failed writing execution meta data", e);
			}
		}
	}

	private File getExecutionMetaFile() {
		return new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER), EXECUTION_FILE_NAME);
	}

	private int getMaxId() {
		int maxId = 0;
		for (Integer i : executionsCache.keySet()) {
			if (i > maxId) {
				maxId = i;
			}
		}
		return maxId;
	}

	private void readExecutionMeta() {
		if (executionsCache != null) {
			// We read it already
			return;
		}
		synchronized (fileAccessLockObject) {
			final File metaFile = getExecutionMetaFile();
			if (!metaFile.exists()) {
				executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetaData>());
				return;
			}
			try {
				executionsCache = new ObjectMapper().readValue(metaFile,
						new TypeReference<Map<Integer, ExecutionMetaData>>() {
						});
				executionsCache = Collections.synchronizedMap(executionsCache);
				for (ExecutionMetaData meta : executionsCache.values()) {
					meta.setActive(false);
				}
			} catch (IOException e) {
				log.error("Failed reading execution meta data file.", e);
				executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetaData>());
				return;
			}

		}
	}

	public ExecutionMetaData getExecutionMetaData(int executionId) {
		readExecutionMeta();
		ExecutionMetaData executionMetaData = executionsCache.get(executionId);
		if (executionMetaData.isActive()) {
			updateSingleExecutionMeta(executionId);
		}
		return new ExecutionMetaData(executionMetaData);
	}

	private void updateSingleExecutionMeta(int executionId) {
		ExecutionMetaData executionMetaData = executionsCache.get(executionId);
		if (executionMetaData.getExecution() == null || executionMetaData.getExecution().getLastMachine() == null) {
			return;
		}
		int numOfTests = 0;
		int numOfSuccessfulTests = 0;
		int numOfFailedTests = 0;
		int numOfTestsWithWarnings = 0;
		int numOfMachines = 0;

		for (MachineNode machine : executionMetaData.getExecution().getMachines()) {
			numOfMachines++;
			final List<ScenarioNode> scenarios = machine.getChildren();
			if (null == scenarios) {
				continue;
			}
			for (ScenarioNode scenario : scenarios) {
				for (Node node : scenario.getChildren(true)) {
					if (node instanceof TestNode) {
						numOfTests++;
						switch (node.getStatus()) {
						case success:
							numOfSuccessfulTests++;
							break;
						case error:
						case failure:
							numOfFailedTests++;
							break;
						case warning:
							numOfTestsWithWarnings++;
						default:
							break;
						}
					}
				}
			}
		}
		synchronized (executionMetaData) {
			executionMetaData.setNumOfTests(numOfTests);
			executionMetaData.setNumOfFailedTests(numOfFailedTests);
			executionMetaData.setNumOfSuccessfulTests(numOfSuccessfulTests);
			executionMetaData.setNumOfTestsWithWarnings(numOfTestsWithWarnings);
			executionMetaData.setNumOfMachines(numOfMachines);
		}

	}

	public ExecutionMetaData[] getAllMetaData() {
		readExecutionMeta();
		final List<ExecutionMetaData> result = new ArrayList<ExecutionMetaData>();
		for (int executionId : executionsCache.keySet()) {
			ExecutionMetaData meta = executionsCache.get(executionId);
			if (meta.isActive()) {
				updateSingleExecutionMeta(executionId);
			}
		}
		result.addAll(executionsCache.values());
		return result.toArray(new ExecutionMetaData[] {});
	}

	public int getSharedExecutionIndexAndAddIfNoneExist(ExecutionDetails executionDescription) {
		readExecutionMeta();
		synchronized (lockObject) {
			if (executionsCache.isEmpty()) {
				log.debug("Execution map is empty, adding execution");
				return addExecution(executionDescription);
			}

			// It is easier to reverse the order of lists then sets, so we
			// convert it.
			// We need to reverse the order so will find the latest executions
			// first.
			List<Integer> indexes = new ArrayList<Integer>();
			indexes.addAll(executionsCache.keySet());
			for (int executionIndex : indexes) {
				final ExecutionMetaData metaData = executionsCache.get(executionIndex);
				if (metaData.isActive() && metaData.isShared()) {
					metaData.setLastAccessedTime(System.currentTimeMillis());
					return executionIndex;
				}
			}
			return addExecution(executionDescription);
		}

	}

	public Execution getExecution(int index) {
		return executionsCache.get(index).getExecution();
	}

	@Override
	public void executionAdded(int executionId, Execution execution) {
		// This method is mostly triggered by this class, so there is not much
		// sense usually to add logic in here.
	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		final ExecutionMetaData metadata = executionsCache.get(executionId);
		if (null == metadata) {
			log.error("Trying to disable execution with id " + executionId + " which is not exist");
		}
		updateMetaData();
		metadata.setActive(false);
		writeExecutionMeta();
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
		updateMetaData();
		writeExecutionMeta();
	}

	/**
	 * Updates the state of all the active executions.
	 */
	private void updateMetaData() {
		for (int executionId : executionsCache.keySet()) {
			final ExecutionMetaData meta = executionsCache.get(executionId);
			if (!meta.active) {
				continue;
			}
			updateSingleExecutionMeta(executionId);
		}

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
		 * The description of the execution as described by the user the
		 * triggered it
		 */
		private String description;

		/**
		 * Free list of properties that can be specified by the user
		 */
		private Map<String, String> properties;

		/**
		 * Is this execution can be shared between different machines.
		 */
		private boolean shared;

		/**
		 * The name of the folder in the file system that holds the report file.
		 * <br>
		 * e.g. exec_4 <br>
		 */
		private String folderName;

		/**
		 * The uri of the index file of the execution report. <br>
		 * e.g. reports/exec_4/index.html
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

		/**
		 * Is the execution is currently active or is it already finished
		 */
		private boolean active;

		/**
		 * The last time in absolute nanoseconds that this execution was
		 * changed. This is used for calculating if the max idle time is over
		 */
		private long lastAccessedTime;

		/**
		 * Overall number of tests in the execution
		 */
		private int numOfTests;

		/**
		 * Number of successful tests in the execution
		 */
		private int numOfSuccessfulTests;

		/**
		 * Number of failed tests in the execution
		 */
		private int numOfFailedTests;

		/**
		 * Number of tests with warnings in the execution
		 */
		private int numOfTestsWithWarnings;

		/**
		 * Number of machines that were reported to this execution
		 */
		private int numOfMachines;

		/**
		 * The date and time in which the execution has started in. e.g.
		 * 2015/05/12 18:17:49
		 */
		private String timestamp;

		@JsonIgnore
		private Execution execution;

		public ExecutionMetaData() {

		}

		/**
		 * Copy constructor
		 * 
		 * @param metaData
		 */
		public ExecutionMetaData(final ExecutionMetaData metaData) {
			if (null != metaData) {
				this.active = metaData.active;
				this.date = metaData.date;
				this.execution = metaData.execution;
				this.folderName = metaData.folderName;
				this.id = metaData.id;
				this.description = metaData.description;
				this.shared = metaData.shared;
				this.properties = metaData.properties;
				this.lastAccessedTime = metaData.lastAccessedTime;
				this.time = metaData.time;
				this.timestamp = metaData.timestamp;
				this.uri = metaData.uri;
				this.numOfTests = metaData.numOfTests;
				this.numOfSuccessfulTests = metaData.numOfSuccessfulTests;
				this.numOfFailedTests = metaData.numOfFailedTests;
				this.numOfTestsWithWarnings = metaData.numOfTestsWithWarnings;
				this.numOfMachines = metaData.numOfMachines;
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

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Map<String, String> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		public boolean isShared() {
			return shared;
		}

		public void setShared(boolean shared) {
			this.shared = shared;
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

		public int getNumOfTests() {
			return numOfTests;
		}

		public void setNumOfTests(int numOfTests) {
			this.numOfTests = numOfTests;
		}

		public int getNumOfSuccessfulTests() {
			return numOfSuccessfulTests;
		}

		public void setNumOfSuccessfulTests(int numOfSuccessfulTests) {
			this.numOfSuccessfulTests = numOfSuccessfulTests;
		}

		public int getNumOfFailedTests() {
			return numOfFailedTests;
		}

		public void setNumOfFailedTests(int numOfFailedTests) {
			this.numOfFailedTests = numOfFailedTests;
		}

		public int getNumOfTestsWithWarnings() {
			return numOfTestsWithWarnings;
		}

		public void setNumOfTestsWithWarnings(int numOfTestsWithWarnings) {
			this.numOfTestsWithWarnings = numOfTestsWithWarnings;
		}

		public int getNumOfMachines() {
			return numOfMachines;
		}

		public void setNumOfMachines(int numOfMachines) {
			this.numOfMachines = numOfMachines;
		}

	}

}
