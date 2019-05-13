package il.co.topq.difido.updater;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Worker {

	private static final Logger log = LoggerFactory.getLogger(Worker.class);

	private static final String EXECUTION_METADATA_QUERY_TEMPLATE = "INSERT INTO EXECUTION_METADATA "
			+ "(ID, COMMENT , DATE , DESCRIPTION , DURATION , FOLDER_NAME , NUM_OF_FAILED_TESTS , NUM_OF_MACHINES , NUM_OF_SUCCESSFUL_TESTS , NUM_OF_TESTS , NUM_OF_TESTS_WITH_WARNINGS , SHARED , TIME , TIMESTAMP , URI )"
			+ " VALUES"
			+ " ( {0},''{1}'',''{2}'',''{3}'',{4},''{5}'',{6},{7},{8},{9},{10},''{11}'',''{12}'',''{13}'',''{14}'');";

	private static final String EXECUTION_STATE_QUERY_TEMPLATE = "INSERT INTO EXECUTION_STATE "
			+ "(ID, ACTIVE , HTML_EXISTS , LOCKED )" + " VALUES " + "({0},{1},{2},{3});";

	private static final String EXECUTION_PROPERTIES_QUERY_TEMPLATE = "INSERT INTO EXECUTION_PROPERTIES "
			+ "(EXECUTION_ID, NAME , VALUE )" + " VALUES " + "({0},''{1}'',''{2}'');";

	// @formatter:on

	private final JdbcTemplate template;
	private final Map<Integer, OldMetadata> data;
	private final boolean parallel;

	public Worker(JdbcTemplate template, Map<Integer, OldMetadata> data, boolean parallel) {
		super();
		this.template = template;
		this.data = data;
		this.parallel = parallel;
	}

	public void work() {
		if (null == data || data.isEmpty()) {
			log.warn("No data found");
			return;
		}
		Stream<OldMetadata> stream = null;
		if (parallel) {
			stream = data.values().parallelStream();
		} else {
			stream = data.values().stream();
		}
		stream.forEach(e -> {
			log.info("Working on execution " + e.getId());

			// @formatter:off
			final String metadataQuery = MessageFormat.format(EXECUTION_METADATA_QUERY_TEMPLATE, 
					Integer.toString(e.getId()),
					e.getComment(),
					e.getDate(),
					e.getDescription(),
					Long.toString(e.getDuration()),
					e.getFolderName(),
					Integer.toString(e.getNumOfFailedTests()),
					Integer.toString(e.getNumOfMachines()),
					Integer.toString(e.getNumOfSuccessfulTests()),
					Integer.toString(e.getNumOfTests()),
					Integer.toString(e.getNumOfTestsWithWarnings()),					
					e.isShared(),
					e.getTime(),
					e.getTimestamp(),
					e.getUri()
					);
			
			log.debug(metadataQuery);
			template.execute(metadataQuery);
			Stream<String> keyStream = null; 
			if (parallel) {
				keyStream = e.getProperties().keySet().parallelStream(); 
			} else {
				keyStream = e.getProperties().keySet().stream();
			}
			
			keyStream.forEach(key -> 	{
				final String propQuery = MessageFormat.format(EXECUTION_PROPERTIES_QUERY_TEMPLATE,Integer.toString(e.getId()), key, e.getProperties().get(key));
				log.debug(propQuery);
				template.execute(propQuery);
			}
			);
			
			// @formatter:on
			final String stateQuery = MessageFormat.format(EXECUTION_STATE_QUERY_TEMPLATE, Integer.toString(e.getId()),
					e.isActive(), e.isHtmlExists(), e.isLocked());
			log.debug(stateQuery);
			template.execute(stateQuery);
		});
	}

}
