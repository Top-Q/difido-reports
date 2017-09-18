package il.co.topq.report.business.execution;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.Common;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionMetadata implements Comparable<ExecutionMetadata> {

	/**
	 * Is the meta data was saved to the persistency.
	 */
	@JsonIgnore
	private boolean dirty;

	/**
	 * The id of the execution
	 */
	private int id;

	/**
	 * The description of the execution as described by the user the triggered
	 * it
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
	 * If execution is locked it will not be deleted from disk no matter how old
	 * it is
	 */
	private boolean locked;

	/**
	 * When the HTML is deleted, the flag is set to false. This can happen if
	 * the execution age is larger then the maximum days allowed.
	 */
	private boolean htmlExists = true;

	/**
	 * The last time in absolute milliseconds that this execution was changed.
	 * This is used for calculating if the max idle time is over. <br>
	 * Marked as @jsonIgnore since there is no need to save it to the file
	 * because when reading from the file the sessions are always none active
	 */
	@JsonIgnore
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
	 * The date and time in which the execution has started in. e.g. 2015/05/12
	 * 18:17:49
	 */
	private String timestamp;

	/**
	 * The duration of the execution in milliseconds.
	 */
	private long duration;

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
		setDirty(true);
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
			this.duration = metaData.duration;
			this.uri = metaData.uri;
			this.numOfTests = metaData.numOfTests;
			this.numOfSuccessfulTests = metaData.numOfSuccessfulTests;
			this.numOfFailedTests = metaData.numOfFailedTests;
			this.numOfTestsWithWarnings = metaData.numOfTestsWithWarnings;
			this.numOfMachines = metaData.numOfMachines;
			this.dirty = metaData.dirty;
		}
	}

	public ExecutionMetadata(String timestamp, Execution execution) {
		this.timestamp = timestamp;
		this.execution = execution;
		this.active = true;
		lastAccessedTime = System.currentTimeMillis();
	}

	/**
	 * Enable to sort collection of this class by descending order of the date
	 * and time
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
			throw new IllegalArgumentException("Can't compare when some fields are null. Trying to compare '"
					+ this.toString() + "' with '" + o.toString() + "'");
		}
		if (getDate().isEmpty() || getTime().isEmpty() || o.getTime().isEmpty() || o.getDate().isEmpty()) {
			throw new IllegalArgumentException("Can't compare when some fields are empty. Trying to compare '"
					+ this.toString() + "' with '" + o.toString() + "'");
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
		} catch (Throwable t) {
			throw new IllegalArgumentException(
					"Exception accured while trying to parse date or time when comparing. Trying to compare '"
							+ this.toString() + "' with '" + o.toString() + "'",
					t);
		}
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("id", id)
				.append("description", description)
				.append("properties", properties)
				.append("shared", shared)
				.append("folderName", folderName)
				.append("uri", uri)
				.append("date", date)
				.append("time", time)
				.append("duration", duration)
				.append("active", active)
				.append("locked", locked)
				.append("htmlExists", htmlExists)
				.append("lastAccessedTime", lastAccessedTime)
				.append("numOfTests", numOfTests)
				.append("numOfSuccessfulTests", numOfSuccessfulTests)
				.append("numOfFailedTests", numOfFailedTests)
				.append("numOfTestsWithWarnings", numOfTestsWithWarnings)
				.append("numOfMachines", numOfMachines)
				.append("timestamp", timestamp)
				.toString();
		// @formatter:on
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
		setDirty(true);
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		setDirty(true);
	}

	public boolean isHtmlExists() {
		return htmlExists;
	}

	public void setHtmlExists(boolean htmlExists) {
		this.htmlExists = htmlExists;
		setDirty(true);
	}

	@JsonIgnore
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@JsonIgnore
	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
		setDirty(true);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		setDirty(true);
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
		setDirty(true);
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
		setDirty(true);
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		setDirty(true);
		this.folderName = folderName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		setDirty(true);
		this.uri = uri;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		setDirty(true);
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		if (time == null || time.equals(this.time)) {
			return;
		}
		setDirty(true);
		this.time = time;
	}

	@JsonIgnore
	public void setExecution(Execution execution) {
		this.execution = execution;
	}

	public void setTimestamp(String timestamp) {
		if (this.timestamp == timestamp) {
			return;
		}
		setDirty(true);
		this.timestamp = timestamp;
	}

	public int getNumOfTests() {
		return numOfTests;
	}

	public void setNumOfTests(int numOfTests) {
		if (this.numOfTests == numOfTests) {
			return;
		}
		setDirty(true);
		this.numOfTests = numOfTests;
	}

	public int getNumOfSuccessfulTests() {
		return numOfSuccessfulTests;
	}

	public void setNumOfSuccessfulTests(int numOfSuccessfulTests) {
		if (this.numOfSuccessfulTests == numOfSuccessfulTests) {
			return;
		}
		setDirty(true);
		this.numOfSuccessfulTests = numOfSuccessfulTests;

	}

	public int getNumOfFailedTests() {
		return numOfFailedTests;
	}

	public void setNumOfFailedTests(int numOfFailedTests) {
		if (numOfFailedTests == this.numOfFailedTests) {
			return;
		}
		setDirty(true);
		this.numOfFailedTests = numOfFailedTests;
	}

	public int getNumOfTestsWithWarnings() {
		return numOfTestsWithWarnings;
	}

	public void setNumOfTestsWithWarnings(int numOfTestsWithWarnings) {
		if (this.numOfTestsWithWarnings == numOfTestsWithWarnings) {
			return;
		}
		setDirty(true);
		this.numOfTestsWithWarnings = numOfTestsWithWarnings;
	}

	public int getNumOfMachines() {
		return numOfMachines;
	}

	public void setNumOfMachines(int numOfMachines) {
		if (this.numOfMachines == numOfMachines) {
			return;
		}
		setDirty(true);
		this.numOfMachines = numOfMachines;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	@JsonIgnore
	public boolean isDirty() {
		return dirty;
	}

	@JsonIgnore
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

}
