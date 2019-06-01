package il.co.topq.report.front.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class ExecutionResourceIT extends AbstractResourceTest {

	@Test
	@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
	public void getGet() throws Exception {
		client.addExecution(new ExecutionDetails());
		assertThat(metadataRepository.count(), equalTo(1L));
	}

	@Test
	@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
	public void testPut() throws Exception {
		ExecutionDetails executionDetails = new ExecutionDetails();
		int executionId = client.addExecution(executionDetails);
		client.endExecution(executionId);
		
	}

}
