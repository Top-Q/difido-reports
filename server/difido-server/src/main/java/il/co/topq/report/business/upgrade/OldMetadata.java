package il.co.topq.report.business.upgrade;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Represents the ExecutionMetadata as it was in the old version.
 * 
 * @author Itai Agmon
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldMetadata implements Comparable<OldMetadata> {

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
	 * The description of the execution as described by the user the
	 * triggered it
	 */
	private String description;

	/**
	 * A comment for the execution that might be added later (after the
	 * execution ended)
	 */
	private String comment;

	/**
	 * Free list of properties that can be specified by the user
	 */
	private HashMap<String, String> properties;

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
	 * The last time in absolute milliseconds that this execution was
	 * changed. This is used for calculating if the max idle time is over.
	 * <br>
	 * Marked as @jsonIgnore since there is no need to save it to the file
	 * because when reading from the file the sessions are always none
	 * active
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
	 * The date and time in which the execution has started in. e.g.
	 * 2015/05/12 18:17:49
	 */
	private String timestamp;

	/**
	 * The duration of the execution in milliseconds.
	 */
	private long duration;

	public OldMetadata() {
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
	public OldMetadata(final OldMetadata metaData) {
		if (null != metaData) {
			this.active = metaData.active;
			this.locked = metaData.locked;
			this.htmlExists = metaData.htmlExists;
			this.date = metaData.date;
			this.folderName = metaData.folderName;
			this.id = metaData.id;
			this.description = metaData.description;
			this.comment = metaData.comment;
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

	/**
	 * Enable to sort collection of this class by descending order of the
	 * date and time
	 */
	@Override
	public int compareTo(OldMetadata o) {
		if (null == o) {
			return 1;
		}
		if (!(o instanceof OldMetadata)) {
			throw new IllegalArgumentException(
					"Can't compare " + this.getClass().getSimpleName() + " to " + o.getClass().getSimpleName());
		}
		if (this == o) {
			return 0;
		}
		if (this.getId() == o.getId()) {
			return 0;
		}
		if (this.getId() > o.getId()) {
			return 1;
		} else {
			return -1;
		}
	}

	public String getTimestamp() {
		return timestamp;
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

	public HashMap<String, String> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, String> properties) {
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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
