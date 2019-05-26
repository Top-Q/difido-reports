package il.co.topq.report.business.upgrade;

import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.DateTimeConverter;
import il.co.topq.report.business.JdbcInserter;

class Worker {

	private static final Logger log = LoggerFactory.getLogger(Worker.class);

	private final JdbcInserter jdbc;
	private final Map<Integer, OldMetadata> data;
	private final boolean parallel;

	Worker(JdbcInserter jdbc, Map<Integer, OldMetadata> data, boolean parallel) {
		super();
		this.jdbc = jdbc;
		this.data = data;
		this.parallel = parallel;
	}

	void work() {
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

			if (null == e.getTimestamp() || e.getTimestamp().isEmpty()) {
				log.warn("Execution " + e.getId() + " has no timestamp. Setting fake time stamp");
				e.setTimestamp("2018/01/01 00:00:00");
			}

			jdbc.insertExecutionMetadata(e.getId(), e.getComment(), e.getDescription(), DateTimeConverter.fromElasticString(e.getTimestamp()).toDateObject(), e.getDuration(), e.getFolderName(), e.getUri(), e.isShared(), e.getNumOfMachines(), e.getNumOfTests(), e.getNumOfSuccessfulTests(), e.getNumOfTestsWithWarnings(), e.getNumOfFailedTests());
			Stream<String> keyStream = null;
			if (e.getProperties() != null) {
				if (parallel) {
					keyStream = e.getProperties().keySet().parallelStream();
				} else {
					keyStream = e.getProperties().keySet().stream();
				}
				keyStream.forEach(key -> {
					jdbc.insertExecutionProperties(e.getId(), key, e.getProperties().get(key));
				});
			}
			jdbc.insertExecutionState(e.getId(), e.isActive(), e.isHtmlExists(), e.isLocked());

		});
	}

}
