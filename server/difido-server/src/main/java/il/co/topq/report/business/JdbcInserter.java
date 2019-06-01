package il.co.topq.report.business;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for writing to the database in cases when it is not
 * possible to use the JPA. The most common scenarios are when upgrading or
 * archiving remote executions and there is a need to use an existing execution
 * id and not to let the database generating one.
 * 
 * 
 * @author Itai Agmon
 *
 */
@Component
public class JdbcInserter {

	private static final Logger log = LoggerFactory.getLogger(JdbcInserter.class);

	@Value("${spring.datasource.url}")
	private String dataSourceUrl;

	@Value("${spring.datasource.username}")
	private String dataUserName;

	@Value("${spring.datasource.password}")
	private String dataPassword;

	private static final String EXECUTION_METADATA_QUERY_TEMPLATE = "INSERT INTO EXECUTION_METADATA "
			+ "(ID, COMMENT , DESCRIPTION , TIMESTAMP, DURATION , FOLDER_NAME , URI , SHARED , NUM_OF_MACHINES , NUM_OF_TESTS, NUM_OF_SUCCESSFUL_TESTS  , NUM_OF_TESTS_WITH_WARNINGS ,NUM_OF_FAILED_TESTS )"
			+ " VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?);";

	private static final String EXECUTION_STATE_QUERY_TEMPLATE = "INSERT INTO EXECUTION_STATE "
			+ "(METADATA_ID, ACTIVE , HTML_EXISTS , LOCKED ) VALUES (?,?,?,?);";

	private static final String EXECUTION_PROPERTIES_QUERY_TEMPLATE = "INSERT INTO EXECUTION_PROPERTIES "
			+ "(METADATA_ID, NAME , VALUE ) VALUES (?,?,?);";

	private JdbcTemplate template;

	@PostConstruct
	public void init() {
		template = buildTemplate();
	}

	public void insertExecutionMetadata(int id, String comment, String description, Date timestamp, long duration,
			String folderName, String uri, boolean shared, int numOfMachines, int numOfTests, int numOfSuccessfulTests,
			int numOfTestsWithWarnings, int numOfFailedTests) {
		template.update(EXECUTION_METADATA_QUERY_TEMPLATE, id, comment, description, timestamp, duration, folderName,
				uri, shared, numOfMachines, numOfTests, numOfSuccessfulTests, numOfTestsWithWarnings, numOfFailedTests);
	}

	public void insertExecutionProperties(int id, String name, String value) {
		template.update(EXECUTION_PROPERTIES_QUERY_TEMPLATE, id, name, value);
	}

	public void insertExecutionState(int id, boolean active, boolean htmlExists, boolean locked) {
		template.update(EXECUTION_STATE_QUERY_TEMPLATE, id, active, htmlExists, locked);
	}

	private JdbcTemplate buildTemplate() {
		log.debug("Building JDBC template with data souruce: " + dataSourceUrl);
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(dataSourceUrl);
		ds.setUser(dataUserName);
		ds.setPassword(dataPassword);
		JdbcTemplate template = new JdbcTemplate(ds);
		return template;
	}
}
