package il.co.topq.report.business.elastic;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.report.Common;

public class ElasticsearchTestIT {

	private int executionId;

	private String executionTimeStamp;

	@Before
	public void setUp() {
		executionId = 10;
		executionTimeStamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
	}

	@Test
	public void testEquals() {
		ElasticsearchTest esTest0 = ElasticsearchTestGenerator.generateEsTest(executionId, executionTimeStamp, "aaa");
		ElasticsearchTest esTest1 = new ElasticsearchTest(esTest0);
		Assert.assertTrue(esTest0.equals(esTest1));
	}

	@Test
	public void testPositiveHashcode() {
		ElasticsearchTest esTest0 = ElasticsearchTestGenerator.generateEsTest(executionId, executionTimeStamp, "aaa");
		ElasticsearchTest esTest1 = new ElasticsearchTest(esTest0);
		Assert.assertTrue(esTest0.hashCode() == esTest1.hashCode());
	}

	@Test
	public void testNegativeHashcode() {
		ElasticsearchTest esTest0 = ElasticsearchTestGenerator.generateEsTest(executionId, executionTimeStamp, "aaa");
		ElasticsearchTest esTest1 = new ElasticsearchTest(esTest0);
		esTest1.setName(esTest1.getName() + "a");
		Assert.assertTrue(esTest0.hashCode() != esTest1.hashCode());
	}

}
