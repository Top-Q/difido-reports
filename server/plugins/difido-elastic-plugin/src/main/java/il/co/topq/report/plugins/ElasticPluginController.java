package il.co.topq.report.plugins;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.elastic.ESClient;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.StopWatch;
import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.MachineCreatedEvent;

/**
 * 
 * @author Itai.Agmon
 *
 */
@Component
public class ElasticPluginController {

	private static final String INDEX_SETTINGS_FILE = "mapping.json";

	private static final String TEST_TYPE = "test";

	private final Logger log = LoggerFactory.getLogger(ElasticPluginController.class);

	volatile Map<Integer, Set<TestNode>> savedTestsPerExecution;

	// TODO: For testing. Of course that this should be handled differently.
	// Probably using the application context.
	public static boolean enabled = true;

	private static boolean storeOnlyAtEnd;

	ESClient client;

	public ElasticPluginController() {
		savedTestsPerExecution = Collections.synchronizedMap(new HashMap<Integer, Set<TestNode>>());
		storeOnlyAtEnd = Configuration.INSTANCE.readBoolean(ConfigProps.STORE_IN_ELASTIC_ONLY_AT_EXECUTION_END);
		log.debug("Store only at end of execution is set to: enabled=" + storeOnlyAtEnd);
		final String host = Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST);
		final int port = Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT);
		client = new ESClient(host, port);
		createIndexIfNoneExists();
		validateElasticStatus();

	}

	@SuppressWarnings("unchecked")
	private void validateElasticStatus() {
		if (!enabled) {
			return;
		}
		Map<String, Object> response = null;
		try {
			response = client.index(Common.ELASTIC_INDEX).stats().asMap();
		} catch (IOException e) {
			log.error("Failed to get the status of the Elatic index due to " + e.getMessage() + ". Disabling Elastic");
			enabled = false;
			return;
		}
		final Map<String, Object> shardsResponse = (Map<String, Object>) response.get("_shards");
		int failedShards = (Integer) shardsResponse.get("failed");
		if (failedShards > 0) {
			log.error("There are " + failedShards + " failed shards in Elastic");
		}
		int totalShards = (Integer) shardsResponse.get("total");
		int successfulShards = (Integer) shardsResponse.get("successful");
		if (totalShards != successfulShards) {
			log.warn("Total number of shards (" + totalShards + ") is not equal to the number of successful shards ("
					+ successfulShards + "). Check that all nodes are available");
		}

	}

	private void createIndexIfNoneExists() {
		if (!enabled) {
			return;
		}
		try {
			if (client.index(Common.ELASTIC_INDEX).isExists()) {
				return;
			}

			// We are reading the mapping from external file and not using the
			// Java API since it seems that it is not possible to do a dynamic
			// mapping using the API
			final File settingsFile = new File(Common.CONFIUGRATION_FOLDER_NAME, INDEX_SETTINGS_FILE);
			if (!settingsFile.exists()) {
				log.error("Failed to find elastic mapping file in " + settingsFile.getAbsolutePath()
						+ ". Will not be able to configure Elastic");
				return;
			}

			String settings = null;
			try {
				settings = FileUtils.readFileToString(settingsFile);
			} catch (IOException e) {
				log.error("Failed to read mapping file. No index mapping will be set to the Elasticsearch", e);
				return;
			}

			client.index(Common.ELASTIC_INDEX).create(settings);

		} catch (IOException e) {
			log.error("Failed to connect to Elasticsearch or to create index due to " + e.getMessage());
			enabled = false;
		}
	}

	@EventListener
	public void onExecutionDeletedEvent(ExecutionDeletedEvent executionDeletedEvent) {
		if (!enabled) {
			return;
		}
		StopWatch stopWatch = new StopWatch(log).start("Deleting all tests of execution with id "
				+ executionDeletedEvent.getMetadata().getId() + " from the Elastic");

		log.debug("About to delete all tests of execution " + executionDeletedEvent.getExecutionId()
				+ " from the ElasticSearch");
		// Delete by filter is not possible anymore so we do the deletion in two
		// parts. First we get all the tests and then we delete it one by one
		// using the id of each one.
		List<ElasticsearchTest> testsToDelete = null;
		try {
//			@formatter:off
			testsToDelete = client
					.index(Common.ELASTIC_INDEX)
					.document(TEST_TYPE)
					.search()
					.byTerm("executionId", String.valueOf(executionDeletedEvent.getExecutionId()))
					.asClass(ElasticsearchTest.class);
//			@formatter:on

		} catch (Exception e) {
			log.error("Failed to get tests to delete for execution " + executionDeletedEvent.getExecutionId(), e);
			return;
		}
		log.debug("Found " + testsToDelete.size() + " tests to delete");
		for (ElasticsearchTest test : testsToDelete) {
			Map<String, Object> response = null;
			try {
//			@formatter:off
				response = client
						.index(Common.ELASTIC_INDEX)
						.document(TEST_TYPE)
						.delete()
						.single(test.getUid());
//			@formatter:on

			} catch (IOException e) {
				log.warn("Failed to delete test with id: " + test.getUid());
				continue;
			}
			if (!"deleted".equals(response.get("result").toString())) {
				log.warn("Test of execution " + executionDeletedEvent.getExecutionId() + " with id " + test.getUid()
						+ " was not found for deletion");

			}
		}
		log.debug("Finished deleting tests of execution " + executionDeletedEvent.getExecutionId()
				+ " from the Elasticsearch");
		stopWatch.stopAndLog();
	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		if (!enabled) {
			return;
		}
		for (MachineNode machineNode : executionEndedEvent.getMetadata().getExecution().getMachines()) {
			saveDirtyTests(executionEndedEvent.getMetadata(), machineNode);
		}
		updateExecutionDuration(executionEndedEvent.getMetadata().getId(),
				executionEndedEvent.getMetadata().getDuration());

		log.debug("Removing all saved test for execution " + executionEndedEvent.getExecutionId() + " from the cache");
		savedTestsPerExecution.remove(executionEndedEvent.getExecutionId());

	}

	/**
	 * Updating all the tests that are included in the execution with the
	 * specified <code>executionId</code> with the specified
	 * <code>duration</code> in milli
	 * 
	 * @param executionId
	 *            The id of the execution with the tests to update
	 * @param duration
	 *            The execution duration in millis
	 */
	private void updateExecutionDuration(int executionId, long duration) {
		StopWatch stopWatch = new StopWatch(log)
				.start("Updating all test tests of execution " + executionId + " with duration " + duration);
		try {
//			@formatter:off
			final List<ElasticsearchTest> tests = client
					.index(Common.ELASTIC_INDEX)
					.document(TEST_TYPE)
					.search()
					.byQuery("executionId:" + executionId)
					.asClass(ElasticsearchTest.class);
//			@formatter:on
			tests.stream().forEach(test -> test.setExecutionDuration(duration));
			String[] ids = tests.stream().map(test -> test.getUid()).toArray(String[]::new);
//			@formatter:off
			client
			.index(Common.ELASTIC_INDEX)
			.document(TEST_TYPE)
			.update()
			.bulk(ids, tests);
//			@formatter:on
		} catch (IOException e) {
			log.warn("Failed to retrieve tests for execution with id " + executionId);
		}
		stopWatch.stopAndLog();
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		if (!enabled || storeOnlyAtEnd) {
			return;
		}
		saveDirtyTests(machineCreatedEvent.getMetadata(), machineCreatedEvent.getMachineNode());
	}

	private void saveDirtyTests(ExecutionMetadata metadata, MachineNode machineNode) {
		StopWatch stopWatch = new StopWatch(log).start("Validating the state of Elasticsearch");
		validateElasticStatus();
		stopWatch.stopAndLog();
		stopWatch.start("Fetching all execution tests");
		final Set<TestNode> executionTests = getExecutionTests(machineNode);
		stopWatch.stopAndLog();
		if (executionTests.isEmpty()) {
			return;
		}

		stopWatch.start("Finding tests that are not updated in the Elastic");
		final Set<TestNode> testsToUpdate = findTestsToUpdate(metadata.getId(), executionTests);
		stopWatch.stopAndLog();

		if (testsToUpdate.isEmpty()) {
			return;
		}

		stopWatch.start("Converting tests to Elastic");
		List<ElasticsearchTest> esTests = convertToElasticTests(metadata, machineNode, testsToUpdate);
		stopWatch.stopAndLog();

		stopWatch.start("Storing tests in Elastic");
		addOrUpdateInElastic(esTests);
		stopWatch.stopAndLog();

		stopWatch.start("Clonning all the updated tests");
		Set<TestNode> clonedTests = cloneTests(executionTests);
		stopWatch.stopAndLog();

		savedTestsPerExecution.put(metadata.getId(), clonedTests);
	}

	Set<TestNode> findTestsToUpdate(int executionId, Set<TestNode> executionTests) {
		if (!savedTestsPerExecution.containsKey(executionId)) {
			// There are no tests in this execution that are already stored in
			// the
			// Elastic
			return executionTests;
		}

		// Get all the tests that were saved in the Elastic
		final Set<TestNode> updatedExecutionTests = savedTestsPerExecution.get(executionId);

		// Remove the updated tests from all the tests
		final Set<TestNode> testsToUpdate = new HashSet<TestNode>(executionTests);
		testsToUpdate.removeAll(updatedExecutionTests);
		log.trace("Found tests to update: " + testsToUpdate);
		return testsToUpdate;
	}

	Set<TestNode> cloneTests(Set<TestNode> executionTests) {
		Set<TestNode> clonedTests = new HashSet<TestNode>();
		for (TestNode test : executionTests) {
			clonedTests.add(new TestNode(test));
		}
		return clonedTests;

	}

	void addOrUpdateInElastic(List<ElasticsearchTest> esTests) {
		log.debug("About to add or update " + esTests.size() + " tests in the Elastic");
		if (null == esTests || esTests.isEmpty()) {
			return;
		}
		String[] ids = new String[esTests.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = esTests.get(i).getUid();
		}
		try {
			client.index(Common.ELASTIC_INDEX).document(TEST_TYPE).add().bulk(ids, esTests);
			Map<String, Object> response = client.index(Common.ELASTIC_INDEX).document(TEST_TYPE).update().bulk(ids,
					esTests);

			if (!"false".equals(response.get("errors").toString())) {
				log.debug("Response: " + response.get("errors"));
				log.error("Failed updating tests in Elastic due to: " + response);
			}
		} catch (Exception e) {
			log.error("Failed to add tests to Elastic due to " + e.getMessage());
		}

	}

	private List<ElasticsearchTest> convertToElasticTests(ExecutionMetadata metadata, MachineNode machineNode,
			Set<TestNode> executionTests) {
		final List<ElasticsearchTest> elasticTests = new ArrayList<ElasticsearchTest>();
		for (TestNode testNode : executionTests) {
			elasticTests.add(testNodeToElasticTest(metadata, machineNode, testNode));
		}
		log.trace("Converted tests to update to Elasticsearch tests " + elasticTests);
		return elasticTests;
	}

	private ElasticsearchTest testNodeToElasticTest(ExecutionMetadata metadata, MachineNode machineNode,
			TestNode testNode) {
		String timestamp = null;
		if (testNode.getTimestamp() != null) {
			timestamp = testNode.getDate() + " " + testNode.getTimestamp();
		} else {
			timestamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
		}
		String executionTimestamp = convertToUtc(metadata.getTimestamp());
		final ElasticsearchTest esTest = new ElasticsearchTest(testNode.getUid(), executionTimestamp,
				convertToUtc(timestamp));
		esTest.setName(testNode.getName());
		esTest.setStatus(testNode.getStatus().name());
		esTest.setDuration(testNode.getDuration());
		esTest.setMachine(machineNode.getName());
		final ScenarioNode rootScenario = machineNode.getChildren().get(machineNode.getChildren().size() - 1);
		if (machineNode.getChildren() != null && !machineNode.getChildren().isEmpty()) {
			esTest.setExecution(rootScenario.getName());
		}
		if (rootScenario.getScenarioProperties() != null) {
			esTest.setScenarioProperties(new HashMap<String, String>(rootScenario.getScenarioProperties()));
		}
		if (testNode.getParent() != null) {
			esTest.setParent(testNode.getParent().getName());
		}
		if (testNode.getDescription() != null) {
			esTest.setDescription(testNode.getDescription());
		}
		if (testNode.getParameters() != null) {
			esTest.setParameters(testNode.getParameters());
		}
		if (testNode.getProperties() != null) {
			esTest.setProperties(testNode.getProperties());
		}
		esTest.setDuration(testNode.getDuration());
		esTest.setStatus(testNode.getStatus().name());
		esTest.setExecutionId(metadata.getId());
		esTest.setUrl(findTestUrl(metadata, testNode.getUid()));
		return esTest;
	}

	Set<TestNode> getExecutionTests(MachineNode machineNode) {
		Set<TestNode> executionTests = new HashSet<TestNode>();
		if (null == machineNode) {
			return executionTests;
		}
		if (null == machineNode.getChildren()) {
			return executionTests;
		}
		for (Node node : machineNode.getChildren(true)) {
			if (node instanceof TestNode) {
				executionTests.add((TestNode) node);
			}
		}
		return executionTests;
	}

	private String findTestUrl(ExecutionMetadata executionMetadata, String uid) {
		if (executionMetadata == null) {
			return "";
		}
		// @formatter:off
		// http://localhost:8080/reports/execution_2015_04_15__21_14_29_767/tests/test_8691429121669-2/test.html
		return "http://" + System.getProperty("server.address") + ":" + System.getProperty("server.port") + "/"
				+ Common.REPORTS_FOLDER_NAME + "/" + executionMetadata.getFolderName() + "/" + "tests" + "/" + "test_"
				+ uid + "/" + "test.html";
		// @formatter:on
	}

	private String convertToUtc(final String dateInLocalTime) {
		try {
			final Date originalDate = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.parse(dateInLocalTime);
			final SimpleDateFormat sdf = (SimpleDateFormat) Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.clone();
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf.format(originalDate);
		} catch (ParseException e) {
			log.warn("Failed to convert date " + dateInLocalTime + " to UTC time zone");
			return dateInLocalTime;
		}
	}

}
