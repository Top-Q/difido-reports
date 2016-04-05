package il.co.topq.report.business.execution;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.ExecutionUpdatedEvent;
import il.co.topq.report.events.FileAddedToTestEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;

@Component
public class MetadataController {

	private final Logger log = LoggerFactory.getLogger(MetadataController.class);

	// Package private for unit testing
	Map<Integer, ExecutionMetadata> executionsCache;;

	private static Object lockObject = new Object();

	private static Object fileAccessLockObject = new Object();

	private static final String EXECUTION_FILE_NAME = "reports/meta.json";

	public ExecutionMetadata addExecution() {
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
	public ExecutionMetadata addExecution(ExecutionDetails executionDetails) {
		readExecutionMeta();
		Execution execution = new Execution();
		final Date executionDate = new Date();
		final ExecutionMetadata metaData = new ExecutionMetadata(
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
			setAllowedPropertiesToMetaData(metaData, executionDetails);
		}
		executionsCache.put(metaData.getId(), metaData);
		writeExecutionMeta();
		return metaData;
	}

	@EventListener
	public void onExecutionDeletionEvent(ExecutionDeletedEvent executionDeletedEvent) {
		// Let's make sure we have the data
		readExecutionMeta();
		deleteExecutionMetadata(executionDeletedEvent.getMetadata());
		writeExecutionMeta();
	}

	@EventListener
	public void onExecutionUpdatedEvent(ExecutionUpdatedEvent executionUpdatedEvent) {
		// Let's make sure we have the data
		readExecutionMeta();
		executionsCache.put(executionUpdatedEvent.getMetadata().getId(), executionUpdatedEvent.getMetadata());
		if (!executionUpdatedEvent.getMetadata().isHtmlExists() && !executionUpdatedEvent.getMetadata().isLocked()) {
			deleteExecutionMetadata(executionUpdatedEvent.getMetadata());
		}
		// We still need to write the metadata, since maybe the model was
		// changed and we need to update the file.
		writeExecutionMeta();
	}

	private void deleteExecutionMetadata(ExecutionMetadata metadata) {
		log.debug("About to delete the metadata of execution with id " + metadata.getId());

		// We need to delete the execution
		if (null == executionsCache.remove(metadata.getId())) {
			log.warn("Tried to delete execution with id " + metadata.getId() + " which is not exists");
		}
		log.debug("Metadata of execution with id " + metadata.getId() + " was deleted");
	}

	/**
	 * Sets the execution properties in the execution meta data. Will allow
	 * addition only of properties that are specified in the configuration file
	 * 
	 * @param metaData
	 * @param executionDetails
	 */
	private void setAllowedPropertiesToMetaData(ExecutionMetadata metaData, ExecutionDetails executionDetails) {
		final List<String> allowedProperties = Configuration.INSTANCE.readList(ConfigProps.CUSTOM_EXECUTION_PROPERTIES);
		if (allowedProperties.isEmpty()) {
			metaData.setProperties(executionDetails.getExecutionProperties());
			return;
		}
		for (String executionProp : executionDetails.getExecutionProperties().keySet()) {
			if (allowedProperties.contains(executionProp)) {
				metaData.addProperty(executionProp, executionDetails.getExecutionProperties().get(executionProp));
			}
		}

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
				executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetadata>());
				return;
			}
			try {
				executionsCache = new ObjectMapper().readValue(metaFile,
						new TypeReference<Map<Integer, ExecutionMetadata>>() {
						});
				executionsCache = Collections.synchronizedMap(executionsCache);
				for (ExecutionMetadata meta : executionsCache.values()) {
					meta.setActive(false);
				}
			} catch (IOException e) {
				log.error("Failed reading execution meta data file.", e);
				executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetadata>());
				return;
			}

		}
	}

	public ExecutionMetadata getExecutionMetadata(int executionId) {
		readExecutionMeta();
		ExecutionMetadata executionMetaData = executionsCache.get(executionId);
		if (null == executionMetaData) {
			log.error("Trying to get execution meta data of execution " + executionId + " which is not exist");
			return null;
		}
		if (executionMetaData.isActive()) {
			updateSingleExecutionMeta(executionId);
		}
		return new ExecutionMetadata(executionMetaData);
	}

	private void updateSingleExecutionMeta(int executionId) {
		ExecutionMetadata executionMetaData = executionsCache.get(executionId);
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

	public ExecutionMetadata[] getAllMetaData() {
		readExecutionMeta();
		final List<ExecutionMetadata> result = new ArrayList<ExecutionMetadata>();
		for (int executionId : executionsCache.keySet()) {
			ExecutionMetadata meta = executionsCache.get(executionId);
			if (meta.isActive()) {
				updateSingleExecutionMeta(executionId);
			}
		}
		result.addAll(executionsCache.values());
		Collections.sort(result);
		return result.toArray(new ExecutionMetadata[] {});
	}

	public ExecutionMetadata getSharedExecutionIndexAndAddIfNoneExist(ExecutionDetails executionDescription) {
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
				final ExecutionMetadata metaData = executionsCache.get(executionIndex);
				if (metaData.isActive() && metaData.isShared()) {
					metaData.setLastAccessedTime(System.currentTimeMillis());
					return metaData;
				}
			}
			return addExecution(executionDescription);
		}

	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		final ExecutionMetadata metadata = executionsCache.get(executionEndedEvent.getExecutionId());
		if (null == metadata) {
			log.error("Trying to disable execution with id " + executionEndedEvent.getExecutionId()
					+ " which is not exist");
		}
		updateMetaData();
		metadata.setActive(false);
		writeExecutionMeta();
	}

	private void updateExecutionLastUpdateTime(int executionId) {
		final ExecutionMetadata metadata = executionsCache.get(executionId);
		if (null == metadata) {
			log.error("Trying to update machine in execution with id " + executionId + " which is not exist");
		}
		metadata.setLastAccessedTime(System.currentTimeMillis());
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		updateExecutionLastUpdateTime(machineCreatedEvent.getExecutionId());
		updateMetaData();
		writeExecutionMeta();
	}

	/**
	 * Updates the state of all the active executions.
	 */
	private void updateMetaData() {
		for (int executionId : executionsCache.keySet()) {
			final ExecutionMetadata meta = executionsCache.get(executionId);
			if (!meta.active) {
				continue;
			}
			updateSingleExecutionMeta(executionId);
		}

	}

	@EventListener
	public void onTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		updateExecutionLastUpdateTime(testDetailsCreatedEvent.getExecutionId());
	}

	public static class ExecutionMetadata implements Comparable<ExecutionMetadata> {
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
		 * If execution is locked it will not be deleted from disk no matter how
		 * old it is
		 */
		private boolean locked;

		/**
		 * When the HTML is deleted, the flag is set to false. This can happen
		 * if the execution age is larger then the maximum days allowed.
		 */
		private boolean htmlExists = true;

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

		public ExecutionMetadata() {

		}

		@JsonIgnore
		public void addProperty(String key, String value) {
			if (null == properties) {
				properties = new HashMap<String, String>();
			}
			properties.put(key, value);
		}

		/**
		 * Copy constructor
		 * 
		 * @param metaData
		 */
		public ExecutionMetadata(final ExecutionMetadata metaData) {
			if (null != metaData) {
				this.active = metaData.active;
				this.locked = metaData.locked;
				this.htmlExists = metaData.htmlExists;
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

		public ExecutionMetadata(String timestamp, Execution execution) {
			this.timestamp = timestamp;
			this.execution = execution;
			this.active = true;
			lastAccessedTime = System.currentTimeMillis();
		}

		/**
		 * Enable to sort collection of this class by descending order of the
		 * date and time
		 */
		@Override
		public int compareTo(ExecutionMetadata o) {
			if (null == o) {
				return 1;
			}
			if (this == o) {
				return 0;
			}
			if (null == getDate() || null == getTime() || null == o.getTime() || null == o.getDate()) {
				throw new IllegalArgumentException("Can't compare when fields are null");
			}
			try {
				final Date thisDate = Common.API_DATE_FORMATTER.parse(getDate());
				final Date otherDate = Common.API_DATE_FORMATTER.parse(o.getDate());
				if (thisDate.before(otherDate)) {
					return 1;
				} else if (thisDate.after(otherDate)) {
					return -1;
				} else {
					final Date thisTime = Common.API_TIME_FORMATTER.parse(getTime());
					final Date otherTime = Common.API_TIME_FORMATTER.parse(o.getTime());
					if (thisTime.before(otherTime)) {
						return 1;
					} else {
						return -1;
					}
				}
			} catch (ParseException e) {
				throw new IllegalArgumentException(
						"Exception accured while trying to parse date or time when comparing");
			}
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

		public boolean isLocked() {
			return locked;
		}

		public void setLocked(boolean locked) {
			this.locked = locked;
		}

		public boolean isHtmlExists() {
			return htmlExists;
		}

		public void setHtmlExists(boolean htmlExists) {
			this.htmlExists = htmlExists;
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

	@EventListener
	public void onFileAddedToTestEvent(FileAddedToTestEvent fileAddedToTestEvent) {
		// There is not much to do when this happens. Only to update the last
		// access time of the execution
		updateExecutionLastUpdateTime(fileAddedToTestEvent.getExecutionId());

	}

}
