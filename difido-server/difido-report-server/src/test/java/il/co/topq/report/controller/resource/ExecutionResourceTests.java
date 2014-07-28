package il.co.topq.report.controller.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

public class ExecutionResourceTests extends AbstractResourceTestCase {

	@Test
	public void testAddExecution() {
		int executionId = client.addExecution();
		assertEquals(0, executionId);
		client.endExecution(executionId);
		executionId = client.addExecution();
		assertEquals(1, executionId);
		client.endExecution(executionId);
	}
	
	@Test
	public void testJoinToExistingExecution() {
		int executionId = client.addExecution();
		int lastExecutionId = client.getLastExecutionId();
		Assert.assertEquals(executionId, lastExecutionId);
		lastExecutionId = client.getLastExecutionId();
		Assert.assertEquals(executionId, lastExecutionId);
		int newExecutionId = client.addExecution();
		Assert.assertEquals(executionId + 1, newExecutionId);
		lastExecutionId = client.getLastExecutionId();
		Assert.assertEquals(newExecutionId, lastExecutionId);
		
	}
	
}
