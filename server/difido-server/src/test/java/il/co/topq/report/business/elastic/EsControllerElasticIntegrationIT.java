package il.co.topq.report.business.elastic;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import il.co.topq.report.Application;
import il.co.topq.report.Common;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionEndedEvent;

@Ignore("Failed when running on Travis CI")
public class EsControllerElasticIntegrationIT {

	private static ESController escontroller;

	private static String executionTimeStamp;

	private int executionId = 1;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		FileUtils.deleteDirectory(new File("data"));
		Application.startElastic();

		// It seems that we need to give the ELastic some time to create the
		// index on slow machines
		Thread.sleep(5000);
		escontroller = new ESController();
		executionTimeStamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());

	}

	@Test	
	public void testAddOrUpdateToElastic() throws Exception {
		List<ElasticsearchTest> tests = ElasticsearchTestGenerator.generateTests(executionId, executionTimeStamp, 10);
		escontroller.addOrUpdateInElastic(tests);
		Thread.sleep(1000);
		List<ElasticsearchTest> storedTests = ESUtils.getAllByQuery(Common.ELASTIC_INDEX, "test",
				ElasticsearchTest.class, "executionId:" + executionId);
		Assert.assertEquals(tests.size(), storedTests.size());
		storedTests.removeAll(tests);
		Assert.assertTrue(storedTests.size() == 0);

	}

	@Test
	public void testAddExecution() throws Exception {
		ExecutionMetadata metaData = ExecutionMetaDataGenerator.generateExecutionMetadata(1, 2, 10);
		ExecutionEndedEvent event = new ExecutionEndedEvent(metaData);
		escontroller.onExecutionEndedEvent(event);
		Thread.sleep(1000);
		List<ElasticsearchTest> storedTests = ESUtils.getAllByQuery(Common.ELASTIC_INDEX, "test",
				ElasticsearchTest.class, "executionId:" + metaData.getId());
		Assert.assertEquals(20, storedTests.size());
	}

	@AfterClass
	public static void teardown() throws IOException {
		Application.stopElastic();
		FileUtils.deleteDirectory(new File("data"));
	}

}
