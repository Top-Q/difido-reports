package il.co.topq.report.controller.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExecutionResourceTests extends AbstractResourceTestCase {
	
	
	@Test
	public void testAddExecution(){
		int executionId = client.addExecution();
		assertEquals(0, executionId);
	}
}
