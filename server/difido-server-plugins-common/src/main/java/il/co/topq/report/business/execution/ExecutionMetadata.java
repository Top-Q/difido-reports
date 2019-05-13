package il.co.topq.report.business.execution;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.JoinColumn;

@Entity
@Cacheable
public class ExecutionMetadata implements Comparable<ExecutionMetadata> {

	/**
	 * The id of the execution
	 */
	@Id
	@GeneratedValue
	private int id;

	/**
	 * The description of the execution as described by the user the triggered
	 * it
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
	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "execution_properties", joinColumns = @JoinColumn(name = "execution_id"))
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

	public ExecutionMetadata() {

	}

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
			this.date = metaData.date;
			this.folderName = metaData.folderName;
			this.id = metaData.id;
			this.description = metaData.description;
			this.comment = metaData.comment;
			this.shared = metaData.shared;
			this.properties = metaData.properties;
			this.time = metaData.time;
			this.timestamp = metaData.timestamp;
			this.duration = metaData.duration;
			this.uri = metaData.uri;
			this.numOfTests = metaData.numOfTests;
			this.numOfSuccessfulTests = metaData.numOfSuccessfulTests;
			this.numOfFailedTests = metaData.numOfFailedTests;
			this.numOfTestsWithWarnings = metaData.numOfTestsWithWarnings;
			this.numOfMachines = metaData.numOfMachines;
		}
	}

	public ExecutionMetadata(String timestamp) {
		this.timestamp = timestamp;
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
		if (!(o instanceof ExecutionMetadata)) {
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

	@Override
	public String toString() {
		// @formatter:off
		return new StringBuilder()
				.append("id"+ id)
				.append("description"+ description)
				.append("comment"+ comment)
				.append("properties"+ properties)
				.append("shared"+ shared)
				.append("folderName"+ folderName)
				.append("uri"+ uri)
				.append("date"+ date)
				.append("time"+ time)
				.append("duration"+ duration)
				.append("numOfTests"+ numOfTests)
				.append("numOfSuccessfulTests"+ numOfSuccessfulTests)
				.append("numOfFailedTests"+ numOfFailedTests)
				.append("numOfTestsWithWarnings"+ numOfTestsWithWarnings)
				.append("numOfMachines"+ numOfMachines)
				.append("timestamp"+ timestamp)
				.toString();
		// @formatter:on
	}

	public String getTimestamp() {
		return timestamp;
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
		if (time == null || time.equals(this.time)) {
			return;
		}
		this.time = time;
	}

	public void setTimestamp(String timestamp) {
		if (this.timestamp == timestamp) {
			return;
		}
		this.timestamp = timestamp;
	}

	public int getNumOfTests() {
		return numOfTests;
	}

	public void setNumOfTests(int numOfTests) {
		if (this.numOfTests == numOfTests) {
			return;
		}
		this.numOfTests = numOfTests;
	}

	public int getNumOfSuccessfulTests() {
		return numOfSuccessfulTests;
	}

	public void setNumOfSuccessfulTests(int numOfSuccessfulTests) {
		if (this.numOfSuccessfulTests == numOfSuccessfulTests) {
			return;
		}
		this.numOfSuccessfulTests = numOfSuccessfulTests;

	}

	public int getNumOfFailedTests() {
		return numOfFailedTests;
	}

	public void setNumOfFailedTests(int numOfFailedTests) {
		if (numOfFailedTests == this.numOfFailedTests) {
			return;
		}
		this.numOfFailedTests = numOfFailedTests;
	}

	public int getNumOfTestsWithWarnings() {
		return numOfTestsWithWarnings;
	}

	public void setNumOfTestsWithWarnings(int numOfTestsWithWarnings) {
		if (this.numOfTestsWithWarnings == numOfTestsWithWarnings) {
			return;
		}
		this.numOfTestsWithWarnings = numOfTestsWithWarnings;
	}

	public int getNumOfMachines() {
		return numOfMachines;
	}

	public void setNumOfMachines(int numOfMachines) {
		if (this.numOfMachines == numOfMachines) {
			return;
		}
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

}
