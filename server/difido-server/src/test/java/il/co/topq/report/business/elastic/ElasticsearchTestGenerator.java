package il.co.topq.report.business.elastic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.topq.report.Common;
import il.co.topq.report.business.elastic.ElasticsearchTest;

class ElasticsearchTestGenerator {

	private ElasticsearchTestGenerator() {
		// static
	}

	static List<ElasticsearchTest> generateTests(int executionId, String executionTimeStamp, final int numOfTests) {
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		for (int i = 0; i < numOfTests; i++) {
			tests.add(generateEsTest(executionId, executionTimeStamp, i + ""));
		}
		return tests;
	}

	static ElasticsearchTest generateEsTest(int executionId, String executionTimeStamp, String uid) {
		ElasticsearchTest test = new ElasticsearchTest(uid, executionTimeStamp,
				Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date()));
		test.setDescription("foo bar");
		test.setDuration(100);
		test.setExecutionId(executionId);
		test.setMachine("localhost");
		test.setName("Test Foo");
		test.setParent("some parent");
		test.setStatus("Success");
		test.setUrl("http://localhost:8080/exec_10/test" + uid);
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("param0", "value0");
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");
		test.setParameters(parameters);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("prop0", "value0");
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		test.setProperties(properties);
		
		Map<String, String> scenarioProps = new HashMap<String, String>();
		scenarioProps.put("sprop0", "value0");
		scenarioProps.put("sprop1", "value1");
		scenarioProps.put("sprop2", "value2");
		test.setScenarioProperties(scenarioProps);


		return test;
	}

}
