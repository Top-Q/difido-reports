package il.co.topq.report.business.elastic;

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

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.StopWatch;
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
public class ESController {

	private static final String TEST_TYPE = "test";

	private final Logger log = LoggerFactory.getLogger(ESController.class);

	volatile Map<Integer, Set<TestNode>> savedTestsPerExecution;

	// TODO: For testing. Of course that this should be handled differently.
	// Probably using the application context.
	public static boolean enabled;

	private static boolean storeOnlyAtEnd;

	public ESController() {
		savedTestsPerExecution = Collections.synchronizedMap(new HashMap<Integer, Set<TestNode>>());
		enabled = Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_ELASTIC_SEARCH);
		log.debug("Elasticsearch is set to: enabled=" + enabled);
		storeOnlyAtEnd = Configuration.INSTANCE.readBoolean(ConfigProps.STORE_IN_ELASTIC_ONLY_AT_EXECUTION_END);
		log.debug("Store only at end of execution is set to: enabled=" + storeOnlyAtEnd);
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
			testsToDelete = ESUtils.getAllByTerm(Common.ELASTIC_INDEX, TEST_TYPE, ElasticsearchTest.class,
					"executionId", String.valueOf(executionDeletedEvent.getExecutionId()));

		} catch (Exception e) {
			log.error("Failed to get tests to delete for execution " + executionDeletedEvent.getExecutionId(), e);
			return;
		}
		log.debug("Found " + testsToDelete.size() + " tests to delete");
		for (ElasticsearchTest test : testsToDelete) {
			DeleteResponse response = ESUtils.delete(Common.ELASTIC_INDEX, TEST_TYPE, test.getUid());
			if (response.getResult() == Result.NOT_FOUND) {
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
		log.debug("Removing all saved test for execution " + executionEndedEvent.getExecutionId() + " from the cache");
		savedTestsPerExecution.remove(executionEndedEvent.getExecutionId());

	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		if (!enabled || storeOnlyAtEnd) {
			return;
		}
		saveDirtyTests(machineCreatedEvent.getMetadata(), machineCreatedEvent.getMachineNode());
	}

	private void saveDirtyTests(ExecutionMetadata metadata, MachineNode machineNode) {
		StopWatch stopWatch = new StopWatch(log).start("Fetching all execution tests");
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
			BulkResponse response = ESUtils.addBulk(Common.ELASTIC_INDEX, TEST_TYPE, ids, esTests);
			if (response.hasFailures()) {
				log.error("Failed updating tests in Elastic");
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
