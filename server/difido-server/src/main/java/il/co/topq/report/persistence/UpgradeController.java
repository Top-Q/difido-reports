package il.co.topq.report.persistence;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;

@Component
public class UpgradeController {
	private static final Logger log = LoggerFactory.getLogger(UpgradeController.class);

	private static final String EXECUTION_FILE_NAME = "reports/meta.json";

	private final MetadataRepository metadataRepository;

	private final ExecutionStateRepository stateRepository;

	private final File metaFile;

	private EntityManager em;

	private JdbcTemplate template;
	
	@Autowired
	public UpgradeController(MetadataRepository metadataRepository, ExecutionStateRepository stateRepository,EntityManager em, JdbcTemplate template) {
		this.metadataRepository = metadataRepository;
		this.stateRepository = stateRepository;
		this.em = em;
		this.template = template;
		metaFile =  new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER), EXECUTION_FILE_NAME);
		upgrade();
	}

	public void upgrade() {
		if (!metaFile.exists()) {
			log.debug("No old meta file found. No need to upgrade");
			return;
		}
		log.info("Found " + EXECUTION_FILE_NAME + " . Starting to upgrade by publishing data to databse");
		Map<Integer, OldMetadata> data = null;
		try {
			data = new ObjectMapper().readValue(metaFile, new TypeReference<Map<Integer, OldMetadata>>() {
			});
		} catch (IOException e) {
			log.error("Failed reading old " + EXECUTION_FILE_NAME + ". Upgrade process will be aborted", e);
			return;
		}
		data.values().stream().forEach(e -> {
			template.execute("INSERT INTO EXECUTION_METADATA (ID, COMMENT , DATE , DESCRIPTION , DURATION , FOLDER_NAME , NUM_OF_FAILED_TESTS , NUM_OF_MACHINES , NUM_OF_SUCCESSFUL_TESTS , NUM_OF_TESTS , NUM_OF_TESTS_WITH_WARNINGS , PROPERTIES , SHARED , TIME , TIMESTAMP , URI ) VALUES (55556,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1);");
//			em.getTransaction().begin();
//			Session session = em.unwrap(Session.class);
//			session.doWork(new Work() {
//
//				@Override
//				public void execute(Connection connection) throws SQLException {
//
//			        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO EXECUTION_METADATA (ID, COMMENT , DATE , DESCRIPTION , DURATION , FOLDER_NAME , NUM_OF_FAILED_TESTS , NUM_OF_MACHINES , NUM_OF_SUCCESSFUL_TESTS , NUM_OF_TESTS , NUM_OF_TESTS_WITH_WARNINGS , PROPERTIES , SHARED , TIME , TIMESTAMP , URI ) VALUES ( 1212,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1);")) {
//			            stmt.executeQuery();
//			        }					
//			        
//				}
//				
//			});
			
//			metadataRepository.
//			final UpgradeExecutionMetadata metadata = new UpgradeExecutionMetadata();
//			metadata.setId(e.getId());
//			metadata.setDate(e.getDate());
//			metadata.setComment(e.getComment());
//			metadata.setDescription(e.getDescription());
//			metadata.setDuration(e.getDuration());
//			metadata.setFolderName(e.getFolderName());
//			metadata.setNumOfFailedTests(e.getNumOfFailedTests());
//			metadata.setNumOfMachines(e.getNumOfMachines());
//			metadata.setNumOfSuccessfulTests(e.getNumOfSuccessfulTests());
//			metadata.setNumOfTests(e.getNumOfTests());
//			metadata.setNumOfTestsWithWarnings(e.getNumOfTestsWithWarnings());
//			metadata.setProperties(new HashMap<>(e.getProperties()));
//			metadata.setShared(e.isShared());
//			metadata.setTime(e.getTime());
//			metadata.setTimestamp(e.getTimestamp());
//			metadata.setUri(e.getUri());

			final ExecutionState state = new ExecutionState();
			state.setId(e.getId());
			state.setActive(false);
			state.setHtmlExists(e.isHtmlExists());
			state.setLocked(e.isLocked());

//			metadataRepository.save(metadata);
//			stateRepository.save(state);

		});
		final File backupMetaFile = new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER), EXECUTION_FILE_NAME+ ".upgrade.backup");
		try {
			Files.move(metaFile.toPath(), backupMetaFile.toPath(), REPLACE_EXISTING);
			log.info("Finished upgrading data. Backup file can be found in " + backupMetaFile.getName());
		} catch (IOException e1) {
			log.error("Failed moving old " + EXECUTION_FILE_NAME + " to backup file ", e1);
		}

	}

	/**
	 * Represents the ExecutionMetadata as it was in the old version.
	 * 
	 * @author Itai Agmon
	 *
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class OldMetadata implements Comparable<OldMetadata> {

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

}