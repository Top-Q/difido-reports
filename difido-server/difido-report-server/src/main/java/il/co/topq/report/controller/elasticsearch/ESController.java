package il.co.topq.report.controller.elasticsearch;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.model.ElasticsearchTest;
import il.co.topq.report.model.ExecutionManager;
import il.co.topq.report.model.ExecutionManager.ExecutionMetaData;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Itai.Agmon
 *
 */
public class ESController implements ResourceChangedListener {

	private final Logger log = LoggerFactory.getLogger(ESController.class);

	private List<ElasticsearchTest> testList;
	private Map<Integer, Date> executionTimestamps;

	public ESController() {
		testList = new CopyOnWriteArrayList<ElasticsearchTest>();
		executionTimestamps = new HashMap<Integer, Date>();
	}

	@Override
	public void executionAdded(int executionId, Execution execution) {
		testList = new CopyOnWriteArrayList<ElasticsearchTest>();
		executionTimestamps.put(executionId, new Date());
	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		testList = new CopyOnWriteArrayList<ElasticsearchTest>();
		executionTimestamps.remove(executionId);
	}

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		if (testList.isEmpty()) {
			return;
		}
		ElasticsearchTest testToRemove = null;
		try {
			for (ElasticsearchTest esTest : testList) {
				final TestNode testNode = machine.findTestNodeById(esTest.getUid());
				if (testNode != null) {
					testToRemove = esTest;
					esTest.setStatus(testNode.getStatus().name());
					esTest.setDuration(testNode.getDuration());
					esTest.setMachine(machine.getName());
					final Date executionDate = executionTimestamps.get(executionId);
					if (executionDate != null) {
						esTest.setExecutionTimeStamp(Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER
								.format(executionDate));
					}
					if (machine.getChildren() != null && !machine.getChildren().isEmpty()) {
						esTest.setExecution(machine.getChildren().get(machine.getChildren().size() - 1).getName());
					}
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
						+ testList.toString());
				return;
			}
			log.debug("Removing test with UID " + testToRemove.getUid() + " from the test list");
			testList.remove(testToRemove);
		}
	}

	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		if (details == null || details.getUid() == null) {
			return;
		}
		for (ElasticsearchTest currentTest : testList) {
			if (currentTest.getUid().trim().equals(details.getUid().trim())) {
				return;
			}
		}

		if (ESUtils.isExist(Common.ELASTIC_INDEX, "test", details.getUid())) {
			log.warn("Test with uid " + details.getUid() + " already exists in the Elasticsearch");
			return;
		}
		ElasticsearchTest esTest = new ElasticsearchTest();
		esTest.setUid(details.getUid());
		esTest.setName(details.getName());
		esTest.setDuration(0);
		esTest.setStatus("In progress");
		esTest.setExecutionId(executionId);
		esTest.setProperties(details.getProperties());
		esTest.setUrl(findTestUrl(executionId, details));
		if (details.getTimeStamp() != null) {
			esTest.setTimeStamp(details.getTimeStamp().replaceFirst(" at ", " "));
			esTest.setExecutionTimeStamp(details.getTimeStamp().replaceFirst(" at ", " "));
		}
		esTest.setDescription(details.getDescription());
		log.debug("Adding test with UID " + esTest.getUid() + " to the test list");
		testList.add(esTest);
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
		return Configuration.INSTANCE.read(ConfigProps.BASE_URI).replace("api/", "") +
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
}
