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
			+ "(ID, ACTIVE , HTML_EXISTS , LOCKED )"
			+ " VALUES "
			+ "({0},{1},{2},{3});";
	
	private static final String EXECUTION_PROPERTIES_QUERY_TEMPLATE = "INSERT INTO EXECUTION_PROPERTIES "
			+ "(ID, NAME , VALUE )"
			+ " VALUES "
			+ "({0},''{1}'',''{2}'');";

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
		Stream<OldMetadata> stream = null;
		if (parallel) {
			stream = data.values().parallelStream();
		} else {
			stream = data.values().stream();
		}
		stream.forEach(e -> {
			log.info("Working on execution " + e.getId());

			// @formatter:off
			String query = MessageFormat.format(EXECUTION_METADATA_QUERY_TEMPLATE, 
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
			
			template.execute(query);

			e.getProperties().keySet().stream().forEach(key -> 	{
				String propQuery = MessageFormat.format(EXECUTION_PROPERTIES_QUERY_TEMPLATE,Integer.toString(e.getId()), key, e.getProperties().get(key));
				log.debug(propQuery);
				template.execute(propQuery);
			}
			);
			
			// @formatter:on
			log.debug(query);
			query = MessageFormat.format(EXECUTION_STATE_QUERY_TEMPLATE, Integer.toString(e.getId()), e.isActive(),
					e.isHtmlExists(), e.isLocked());
			log.debug(query);
			template.execute(query);
		});
	}
	
}
