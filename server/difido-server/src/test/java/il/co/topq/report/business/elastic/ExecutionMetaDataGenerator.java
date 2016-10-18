package il.co.topq.report.business.elastic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.business.execution.ExecutionMetadata;

class ExecutionMetaDataGenerator {

	static int index = 0;
	
	static int executionId = 0;

	private ExecutionMetaDataGenerator() {
	}

	static TestNode generateTestNode(String uid) {
		TestNode test = new TestNode("myTest", uid);
		test.setIndex(index++);
		test.setClassName("SomeClass");
		test.setDate("2016/10/01");
		test.setDescription("Aswesome test");
		test.setDuration(232l);
		Map<String, String> params = new HashMap<String, String>();
		params.put("param0", "val0");
		params.put("param1", "val1");
		test.setParameters(params);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("prop0", "val0");
		properties.put("prop1", "val1");
		test.setProperties(properties);
		test.setStatus(Status.success);
		test.setTimestamp("16:59:30");
		return test;
	}

	static ScenarioNode generateScenarioNode(int numOfTests) {
		ScenarioNode scenario = new ScenarioNode("FooScenario");
		scenario.setStatus(Status.success);
		for (int i = 0; i < numOfTests; i++) {
			scenario.addChild(generateTestNode(new Random().nextInt() + i + ""));
		}
		return scenario;
	}

	static MachineNode generateMachineNode(int numOfScenarios, int numOfTestsInScenario) {
		MachineNode machine = new MachineNode("FooMachine");
		machine.setStatus(Status.success);
		for (int i = 0 ; i < numOfScenarios ; i++){
			machine.addChild(generateScenarioNode(numOfTestsInScenario));
		}
		return machine;
	}

	static Execution generateExecution(int numOfMachines, int numOfScenariosInMachine, int numOfTestsInScneario) {
		Execution execution = new Execution();
		for (int i = 0 ; i < numOfMachines; i++){
			execution.addMachine(generateMachineNode(numOfScenariosInMachine,numOfTestsInScneario));
		}
		return execution;
	}
	
	static ExecutionMetadata generateExecutionMetadata(int numOfMachinesInExecution, int numOfScenariosInMachine, int numOfTestsInScneario){
		ExecutionMetadata executionMetaData = new ExecutionMetadata();
		executionMetaData.setId(executionId++);
		executionMetaData.setExecution(generateExecution(numOfMachinesInExecution, numOfScenariosInMachine, numOfTestsInScneario));
		executionMetaData.setActive(true);
		executionMetaData.setDate("16/10/2016");
		executionMetaData.setFolderName(new File(".").getAbsolutePath());
		executionMetaData.setHtmlExists(true);
		executionMetaData.setLocked(false);
		executionMetaData.setNumOfFailedTests(0);
		executionMetaData.setNumOfMachines(numOfMachinesInExecution);
		executionMetaData.setNumOfSuccessfulTests(numOfMachinesInExecution * numOfScenariosInMachine * numOfTestsInScneario);
		executionMetaData.setNumOfTests(numOfMachinesInExecution * numOfScenariosInMachine * numOfTestsInScneario);
		executionMetaData.setNumOfTestsWithWarnings(0);
		executionMetaData.setTime("12:32:11:23");
		executionMetaData.setTimestamp("2016/05/12 18:17:49");
		return executionMetaData;
	}

}
