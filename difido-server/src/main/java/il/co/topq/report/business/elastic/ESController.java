package il.co.topq.report.business.elastic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionCreatedEvent;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;

/**
 * 
 * @author Itai.Agmon
 *
 */
@Component
public class ESController {

	private static final String TEST_TYPE = "test";

	private final Logger log = LoggerFactory.getLogger(ESController.class);

	private Map<Integer, List<ElasticsearchTest>> openTestsPerExecution;

	// TODO: For testing. Of course that this should be handled differently.
	// Probably using the application context.
	public static boolean enabled = true;

	public ESController() {
		openTestsPerExecution = new HashMap<Integer, List<ElasticsearchTest>>();
	}

	@EventListener
	public void onExecutionCreatedEvent(ExecutionCreatedEvent executionCreatedEvent) {
		if (!enabled) {
			return;
		}
		openTestsPerExecution.put(executionCreatedEvent.getExecutionId(),
				new CopyOnWriteArrayList<ElasticsearchTest>());
	}

	@EventListener
	public void onExecutionDeletedEvent(ExecutionDeletedEvent executionDeletedEvent) {
		if (!enabled) {
			return;
		}

		log.debug("About to delete all tests of execution " + executionDeletedEvent.getExecutionId()
				+ " from the ElasticSearch");
		// Delete by filter is not possible anymore so we do the deletion in two
		// parts. First we get all the tests and then we delete it one by one
		// using the id of each one.
		List<ElasticsearchTest> testsToDelete = null;
		try {
			testsToDelete = ESUtils.getAll(Common.ELASTIC_INDEX, TEST_TYPE, ElasticsearchTest.class, "executionId",
					String.valueOf(executionDeletedEvent.getExecutionId()));

		} catch (Exception e) {
			log.error("Failed to get tests to delete for execution " + executionDeletedEvent.getExecutionId(), e);
			return;
		}
		log.debug("Found " + testsToDelete.size() + " tests to delete");
		for (ElasticsearchTest test : testsToDelete) {
			DeleteResponse response = ESUtils.delete(Common.ELASTIC_INDEX, TEST_TYPE, test.getUid());
			if (!response.isFound()) {
				log.warn("Test of execution " + executionDeletedEvent.getExecutionId() + " with id " + test.getUid()
						+ " was not found for deletion");
			}
		}
		log.debug("Finished deleting tests of execution " + executionDeletedEvent.getExecutionId()
				+ " from the Elasticsearch");
	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		if (!enabled) {
			return;
		}

		openTestsPerExecution.remove(executionEndedEvent.getExecutionId());
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

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		if (!enabled) {
			return;
		}

		// This method is called at the start of each test and at the end of
		// each test. Now we need to get the correct test from the machine
		if (openTestsPerExecution == null || openTestsPerExecution.get(machineCreatedEvent.getExecutionId()) == null
				|| openTestsPerExecution.get(machineCreatedEvent.getExecutionId()).isEmpty()) {
			return;
		}
		ElasticsearchTest testToRemove = null;
		try {
			for (ElasticsearchTest esTest : openTestsPerExecution.get(machineCreatedEvent.getExecutionId())) {
				final TestNode testNode = machineCreatedEvent.getMachineNode().findTestNodeById(esTest.getUid());
				if (testNode != null) {
					testToRemove = esTest;
					esTest.setStatus(testNode.getStatus().name());
					esTest.setDuration(testNode.getDuration());
					esTest.setMachine(machineCreatedEvent.getMachineNode().getName());
					final ScenarioNode rootScenario = machineCreatedEvent.getMachineNode().getChildren()
							.get(machineCreatedEvent.getMachineNode().getChildren().size() - 1);
					if (machineCreatedEvent.getMachineNode().getChildren() != null
							&& !machineCreatedEvent.getMachineNode().getChildren().isEmpty()) {
						esTest.setExecution(rootScenario.getName());
					}
					if (rootScenario.getScenarioProperties() != null) {
						esTest.setScenarioProperties(new HashMap<String, String>(rootScenario.getScenarioProperties()));
					}

					// We are losing in the way the testNode parent, so we can't
					// fully support the container properties as intended in
					// JSystem. We will support only container properties that
					// were added to the root scenario.
					// @formatter:off
					// ScenarioNode scenario;
					// while (testNode.getParent() != null &&
					// !(testNode.getParent() instanceof MachineNode)) {
					// scenario = (ScenarioNode) testNode.getParent();
					// if (scenario.getScenarioProperties() != null) {
					// if (esTest.getExecutionProperties() == null) {
					// esTest.setExecutionProperties(new HashMap<String,
					// String>());
					// }
					// esTest.getExecutionProperties().putAll(scenario.getScenarioProperties());
					// }
					// }
					// @formatter:off

					if (testNode.getParent() != null) {
						esTest.setParent(testNode.getParent().getName());
					}
					ESUtils.update(Common.ELASTIC_INDEX, TEST_TYPE, esTest.getUid(), esTest);
				}
			}
		} catch (Exception e) {
			log.error("Failed updating resource", e);
		} finally {
			if (null == testToRemove) {
				log.warn("Trying to remove test from the list but it was null. Test list status: "
						+ openTestsPerExecution.get(machineCreatedEvent.getExecutionId()).toString());
				return;
			}
			log.debug("Removing test with UID " + testToRemove.getUid() + " from the test list");
			openTestsPerExecution.get(machineCreatedEvent.getExecutionId()).remove(testToRemove);
		}
	}

	@EventListener
	public void OnTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		if (!enabled) {
			return;
		}

		if (testDetailsCreatedEvent.getTestDetails() == null
				|| testDetailsCreatedEvent.getTestDetails().getUid() == null || openTestsPerExecution == null) {
			return;
		}
		final TestDetails testDetails = testDetailsCreatedEvent.getTestDetails();
		final int executionId = testDetailsCreatedEvent.getExecutionId();
		for (ElasticsearchTest currentTest : openTestsPerExecution.get(executionId)) {
			if (null == currentTest) {
				continue;
			}
			if (currentTest.getUid().trim().equals(testDetails.getUid().trim())) {
				// We already updated the test, so most of the data is already
				// in the ES. The only data that is interesting and maybe was
				// updated is the test properties, so we are going to check it.
				if (testDetails.getProperties() != null) {
					if (currentTest.getProperties() == null
							|| currentTest.getProperties().size() != testDetails.getProperties().size()) {
						currentTest.setProperties(testDetailsCreatedEvent.getTestDetails().getProperties());
						try {
							ESUtils.update(Common.ELASTIC_INDEX, TEST_TYPE, currentTest.getUid(), currentTest);
						} catch (ElasticsearchException | JsonProcessingException e) {
							log.error("Failed updating test details in the Elasticsearch", e);
						}
					}
				}
				return;
			}
		}

		if (ESUtils.isExist(Common.ELASTIC_INDEX, "test", testDetails.getUid())) {
			log.warn("Test with uid " + testDetails.getUid() + " already exists in the Elasticsearch");
			return;
		}
		String timestamp = null;
		if (testDetails.getTimeStamp() != null) {
			timestamp = testDetails.getTimeStamp().replaceFirst(" at ", " ");
		} else {
			timestamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
		}
		String executionTimestamp = convertToUtc(testDetailsCreatedEvent.getMetadata().getTimestamp());
		final ElasticsearchTest esTest = new ElasticsearchTest(testDetails.getUid(), executionTimestamp,
				convertToUtc(timestamp));
		esTest.setName(testDetails.getName());
		esTest.setDuration(0);
		esTest.setStatus("In progress");
		esTest.setExecutionId(executionId);
		esTest.setProperties(testDetails.getProperties());
		esTest.setUrl(findTestUrl(testDetailsCreatedEvent.getMetadata(), testDetails));
		esTest.setDescription(testDetails.getDescription());
		esTest.setParameters(testDetails.getParameters());
		log.debug("Adding test with UID " + esTest.getUid() + " to the test list");
		openTestsPerExecution.get(executionId).add(esTest);
		IndexResponse indexResponse = null;
		try {
			indexResponse = ESUtils.add(Common.ELASTIC_INDEX, "test", esTest.getUid(), esTest);
		} catch (Throwable t) {
			log.error("Failed adding test with UID " + esTest.getUid(), t);
			return;

		}
		if (indexResponse == null || !indexResponse.isCreated()) {
			log.warn("Test with id " + esTest.getUid() + " is already exists");
		}
	}

	private String findTestUrl(ExecutionMetadata executionMetadata, TestDetails details) {
		if (executionMetadata == null) {
			return "";
		}
		// @formatter:off
		// http://localhost:8080/reports/execution_2015_04_15__21_14_29_767/tests/test_8691429121669-2/test.html
		return "http://" + System.getProperty("server.address") + ":" + System.getProperty("server.port") + "/"
				+ Common.REPORTS_FOLDER_NAME + "/" + executionMetadata.getFolderName() + "/" + "tests" + "/" + "test_"
				+ details.getUid() + "/" + "test.html";
		// @formatter:on
	}

}
