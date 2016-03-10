package il.co.topq.report.listener.elastic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.listener.ResourceChangedListener;
import il.co.topq.report.listener.execution.ExecutionManager;
import il.co.topq.report.listener.execution.ExecutionManager.ExecutionMetaData;

/**
 * 
 * @author Itai.Agmon
 *
 */
public class ESController implements ResourceChangedListener {

	private final Logger log = LoggerFactory.getLogger(ESController.class);

	private Map<Integer, List<ElasticsearchTest>> openTestsPerExecution;

	public ESController() {
		openTestsPerExecution = new HashMap<Integer, List<ElasticsearchTest>>();
	}

	@Override
	public void executionAdded(int executionId, Execution execution) {
		openTestsPerExecution.put(executionId, new CopyOnWriteArrayList<ElasticsearchTest>());
	}

	@Override
	public void executionDeleted(int executionId) {
		log.debug("About to delete all tests of execution " + executionId + " from the ElasticSearch");
		if (ESUtils.delete(Common.ELASTIC_INDEX, "test", "executionId = " + executionId).status() != RestStatus.OK) {
			log.error("Failed deleting all tests of execution with id " + executionId + " from the ElasticSearch");
		}
		log.debug("All tests of execution with id "+ executionId + " were deleted");
	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		openTestsPerExecution.remove(executionId);
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

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		// This method is called at the start of each test and at the end of
		// each test. Now we need to get the correct test from the machine
		if (openTestsPerExecution == null || openTestsPerExecution.get(executionId) == null
				|| openTestsPerExecution.get(executionId).isEmpty()) {
			return;
		}
		ElasticsearchTest testToRemove = null;
		try {
			for (ElasticsearchTest esTest : openTestsPerExecution.get(executionId)) {
				final TestNode testNode = machine.findTestNodeById(esTest.getUid());
				if (testNode != null) {
					testToRemove = esTest;
					esTest.setStatus(testNode.getStatus().name());
					esTest.setDuration(testNode.getDuration());
					esTest.setMachine(machine.getName());
					final ScenarioNode rootScenario = machine.getChildren().get(machine.getChildren().size() - 1);
					if (machine.getChildren() != null && !machine.getChildren().isEmpty()) {
						esTest.setExecution(rootScenario.getName());
					}
					if (rootScenario.getScenarioProperties() != null) {
						esTest.setScenarioProperties(new HashMap<String, String>(rootScenario.getScenarioProperties()));
					}

					// We are losing in the way the testNode parent, so we can't
					// fully support the container properties as intended in
					// JSystem. We will support only container properties that
					// were added to the root scenario.
//					@formatter:off
//					ScenarioNode scenario;
//					while (testNode.getParent() != null && !(testNode.getParent() instanceof MachineNode)) {
//						scenario = (ScenarioNode) testNode.getParent();
//						if (scenario.getScenarioProperties() != null) {
//							if (esTest.getExecutionProperties() == null) {
//								esTest.setExecutionProperties(new HashMap<String, String>());
//							}
//							esTest.getExecutionProperties().putAll(scenario.getScenarioProperties());
//						}
//					}
//					@formatter:off

					if (testNode.getParent() != null) {
						esTest.setParent(testNode.getParent().getName());
					}
					ESUtils.update(Common.ELASTIC_INDEX, "test", esTest.getUid(), esTest);
				}
			}
		} catch (Exception e) {
			log.error("Failed updating resource", e);
		} finally {
			if (null == testToRemove) {
				log.warn("Trying to remove test from the list but it was null. Test list status: "
						+ openTestsPerExecution.get(executionId).toString());
				return;
			}
			log.debug("Removing test with UID " + testToRemove.getUid() + " from the test list");
			openTestsPerExecution.get(executionId).remove(testToRemove);
		}
	}

	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		if (details == null || details.getUid() == null || openTestsPerExecution == null) {
			return;
		}
		for (ElasticsearchTest currentTest : openTestsPerExecution.get(executionId)) {
			if (null == currentTest) {
				continue;
			}
			if (currentTest.getUid().trim().equals(details.getUid().trim())) {
				// We already updated the test, so most of the data is already
				// in the ES. The only data that is interesting and maybe was
				// updated is the test properties, so we are going to check it.
				if (details.getProperties() != null) {
					if (currentTest.getProperties() == null
							|| currentTest.getProperties().size() != details.getProperties().size()) {
						currentTest.setProperties(details.getProperties());
						try {
							ESUtils.update(Common.ELASTIC_INDEX, "test", currentTest.getUid(), currentTest);
						} catch (ElasticsearchException | JsonProcessingException e) {
							log.error("Failed updating test details in the Elasticsearch", e);
						}
					}
				}
				return;
			}
		}

		if (ESUtils.isExist(Common.ELASTIC_INDEX, "test", details.getUid())) {
			log.warn("Test with uid " + details.getUid() + " already exists in the Elasticsearch");
			return;
		}
		String timestamp = null;
		if (details.getTimeStamp() != null) {
			timestamp = details.getTimeStamp().replaceFirst(" at ", " ");
		} else {
			timestamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
		}
		String executionTimestamp = convertToUtc(ExecutionManager.INSTANCE.getExecutionMetaData(executionId)
				.getTimestamp());
		final ElasticsearchTest esTest = new ElasticsearchTest(details.getUid(), executionTimestamp,
				convertToUtc(timestamp));
		esTest.setName(details.getName());
		esTest.setDuration(0);
		esTest.setStatus("In progress");
		esTest.setExecutionId(executionId);
		esTest.setProperties(details.getProperties());
		esTest.setUrl(findTestUrl(executionId, details));
		esTest.setDescription(details.getDescription());
		esTest.setParameters(details.getParameters());
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

	private String findTestUrl(int executionId, TestDetails details) {
		final ExecutionMetaData executionMetadata = ExecutionManager.INSTANCE.getExecutionMetaData(executionId);
		if (executionMetadata == null) {
			return "";
		}
		//@formatter:off
		//http://localhost:8080/reports/execution_2015_04_15__21_14_29_767/tests/test_8691429121669-2/test.html
		return "http://"+System.getProperty("server.address")+":" + System.getProperty("server.port") +
				"/"+
				Common.REPORTS_FOLDER_NAME +
				"/"+
				executionMetadata.getFolderName() +
				"/"+
				"tests" + 
				"/"+
				"test_" +
				details.getUid() +
				"/" +
				"test.html";
		//@formatter:on
	}

	@Override
	public void fileAddedToTest(int executionId, String testUid, byte[] fileContent, String fileName) {
		// We don't store files in Elastic
	}

}
