package il.co.topq.report.front.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class ExecutionResourceIT extends AbstractResourceTest {

	@Test
	public void getGet() throws Exception {
		client.addExecution(new ExecutionDetails());
		assertThat(metadataRepository.count(), equalTo(1L));
	}

	@Test
	public void testPut() throws Exception {
		ExecutionDetails executionDetails = new ExecutionDetails();
		int executionId = client.addExecution(executionDetails);
		client.endExecution(executionId);
		
	}

}
