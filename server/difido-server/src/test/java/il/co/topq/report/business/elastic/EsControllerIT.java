package il.co.topq.report.business.elastic;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.TestNode;

public class EsControllerIT {

	private static ESController controller;

	@Before
	public void setup() {
		controller = new ESController();
	}

	@Test
	public void testGetExecutionTests() {
		MachineNode machine = ExecutionMetaDataGenerator.generateMachineNode(2, 10);
		Set<TestNode> tests = controller.getExecutionTests(machine);
		Assert.assertEquals(tests.size(), 20);
	}

	@Test
	public void testCloneTests() {
		MachineNode machine = ExecutionMetaDataGenerator.generateMachineNode(2, 10);
		Set<TestNode> tests = controller.getExecutionTests(machine);
		Set<TestNode> clonedTests = controller.cloneTests(tests);
		Assert.assertNotNull(clonedTests);
		Assert.assertEquals(tests.size(), clonedTests.size());
		for (TestNode test : tests) {
			Assert.assertTrue(clonedTests.contains(test));
		}
	}
	
	@Test
	public void testFindSingleTestToAdd() {
		MachineNode machine = ExecutionMetaDataGenerator.generateMachineNode(2, 10);
		Set<TestNode> tests = controller.getExecutionTests(machine);
		Set<TestNode> clonedTests = controller.cloneTests(tests);
		controller.savedTestsPerExecution.put(1, clonedTests);
		TestNode newTestNode = ExecutionMetaDataGenerator.generateTestNode(System.currentTimeMillis() + "");
		tests.add(newTestNode);
		Set<TestNode> testsToUpdate = controller.findTestsToUpdate(1, tests);
		Assert.assertEquals(1, testsToUpdate.size());
		Assert.assertTrue(testsToUpdate.contains(newTestNode));
	}
	
	@Test
	public void testFindSingleTestToUpdate() {
		MachineNode machine = ExecutionMetaDataGenerator.generateMachineNode(2, 10);
		Set<TestNode> tests = controller.getExecutionTests(machine);
		Set<TestNode> clonedTests = controller.cloneTests(tests);
		controller.savedTestsPerExecution.put(1, clonedTests);
		for (TestNode test : tests){
			test.addProperty("foo", "bar");
			break;
		}
		Set<TestNode> testsToUpdate = controller.findTestsToUpdate(1, tests);
		Assert.assertEquals(1, testsToUpdate.size());
	}
	
	@Test
	public void testFindAllTestsToAdd() {
		MachineNode machine = ExecutionMetaDataGenerator.generateMachineNode(2, 10);
		Set<TestNode> tests = controller.getExecutionTests(machine);
		Set<TestNode> testsToUpdate = controller.findTestsToUpdate(1, tests);
		Assert.assertEquals(20, testsToUpdate.size());
	}
	
	@Test
	public void testFindAllTestsToUpdate() {
		MachineNode machine = ExecutionMetaDataGenerator.generateMachineNode(2, 10);
		Set<TestNode> tests = controller.getExecutionTests(machine);
		Set<TestNode> clonedTests = controller.cloneTests(tests);
		controller.savedTestsPerExecution.put(1, clonedTests);
		for (TestNode test : tests){
			test.addProperty("foo", "bar");
		}
		Set<TestNode> testsToUpdate = controller.findTestsToUpdate(1, tests);
		Assert.assertEquals(20, testsToUpdate.size());

	}




}
