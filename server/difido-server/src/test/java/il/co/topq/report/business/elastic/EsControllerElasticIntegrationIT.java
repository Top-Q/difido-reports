package il.co.topq.report.business.elastic;

import static il.co.topq.difido.DateTimeConverter.fromNowDateObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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

		// It seems that we need to give the ELastic some time to create the
		// index on slow machines
		Thread.sleep(5000);
		escontroller = new ESController();
		executionTimeStamp = fromNowDateObject().toElasticString();

	}

	@Test	
	public void testAddOrUpdateToElastic() throws Exception {
		List<ElasticsearchTest> tests = ElasticsearchTestGenerator.generateTests(executionId, executionTimeStamp, 10);
		escontroller.addOrUpdateInElastic(tests);
		Thread.sleep(1000);
		
//		@formatter:off
		List<ElasticsearchTest> storedTests = escontroller
				.client
				.index(Common.ELASTIC_INDEX)
				.document("test")
				.search()
				.byTerm("executionId", executionId +"" ).asClass(ElasticsearchTest.class);
//		@formatter:on
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
//		@formatter:off
		List<ElasticsearchTest> storedTests = escontroller
				.client
				.index(Common.ELASTIC_INDEX)
				.document("test")
				.search()
				.byTerm("executionId", metaData.getId() +"" )
				.asClass(ElasticsearchTest.class);
//		@formatter:on

		Assert.assertEquals(20, storedTests.size());
	}

	@AfterClass
	public static void teardown() throws IOException {
		escontroller.client.close();
		FileUtils.deleteDirectory(new File("data"));
	}

}
