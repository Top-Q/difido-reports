package il.co.topq.report.business.execution;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Cacheable
@Table(name = "EXECUTION_METADATA")
public class ExecutionMetadata implements Comparable<ExecutionMetadata> {

	/**
	 * The id of the execution
	 */
	@Id
	@GeneratedValue
	@Column(name = "ID")
	private int id;

	/**
	 * The description of the execution as described by the user the triggered
	 * it
	 */
	@Column(name = "DESCRIPTION")
	private String description;

	/**
	 * A comment for the execution that might be added later (after the
	 * execution ended)
	 */
	@Column(name = "COMMENT")
	private String comment;

	/**
	 * Free list of properties that can be specified by the user
	 */
	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value", length = 1024)
	@CollectionTable(name = "execution_properties", joinColumns = @JoinColumn(name = "metadata_id"))
	private Map<String, String> properties;

	/**
	 * Is this execution can be shared between different machines.
	 */
	@Column(name = "SHARED")
	private boolean shared;

	/**
	 * The name of the folder in the file system that holds the report file.
	 * <br>
	 * e.g. exec_4 <br>
	 */
	@Column(name = "FOLDER_NAME")
	private String folderName;

	/**
	 * The uri of the index file of the execution report. <br>
	 * e.g. reports/exec_4/index.html
	 */
	@Column(name = "URI")
	private String uri;

	/**
	 * Overall number of tests in the execution
	 */

	@Column(name = "NUM_OF_TESTS")
	private int numOfTests;

	/**
	 * Number of successful tests in the execution
	 */
	@Column(name = "NUM_OF_SUCCESSFUL_TESTS")
	private int numOfSuccessfulTests;

	/**
	 * Number of failed tests in the execution
	 */
	@Column(name = "NUM_OF_FAILED_TESTS")
	private int numOfFailedTests;

	/**
	 * Number of tests with warnings in the execution
	 */
	@Column(name = "NUM_OF_TESTS_WITH_WARNINGS")
	private int numOfTestsWithWarnings;

	/**
	 * Number of machines that were reported to this execution
	 */
	@Column(name = "NUM_OF_MACHINES")
	private int numOfMachines;

	/**
	 * The date and time in which the execution has started in. e.g. 2015/05/12
	 * 18:17:49
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIMESTAMP")
	private Date timestamp;

	/**
	 * The duration of the execution in milliseconds.
	 */
	@Column(name = "DURATION")
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
	 * @param metaData Original metadata object
	 */
	public ExecutionMetadata(final ExecutionMetadata metaData) {
		if (null != metaData) {
			this.folderName = metaData.folderName;
			this.id = metaData.id;
			this.description = metaData.description;
			this.comment = metaData.comment;
			this.shared = metaData.shared;
			this.properties = metaData.properties;
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

	public ExecutionMetadata(Date timestamp) {
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

	public Date getTimestamp() {
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

	public void setTimestamp(Date timestamp) {
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
