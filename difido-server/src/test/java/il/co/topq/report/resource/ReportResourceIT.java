package il.co.topq.report.resource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.listener.execution.AbstractResourceTest;
import il.co.topq.report.listener.execution.ExecutionManager.ExecutionMetaData;
import il.co.topq.report.resource.ReportResource.DataTable;

public class ReportResourceIT extends AbstractResourceTest {

	private Client jerseyClient;
	private WebTarget baseTarget;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		String baseUri = "http://0.0.0.0:8080/api/";
		jerseyClient = ClientBuilder.newClient();
		jerseyClient.register(JacksonFeature.class);
		jerseyClient.register(MultiPartWriter.class);
		this.baseTarget = jerseyClient.target(baseUri);
		jerseyClient.target(baseUri);
		AddExecutions();

	}

	private void AddExecutions() throws Exception {
		for (int i = 0; i < 10; i++) {
			Thread.sleep(1000);
		}
	}

	private DataTable getExecutionMetaData() {
		WebTarget executionsTarget = baseTarget.path("/reports");
		DataTable table = executionsTarget.request(MediaType.APPLICATION_JSON)
				.get(DataTable.class);
		return table;
	}

	@Test
	public void testGetReports() throws Exception {
		ExecutionDetails execution = new ExecutionDetails();
		execution.setShared(false);
		client.addExecution(execution);
		DataTable table = getExecutionMetaData();
		Assert.assertNotNull(table);
	}

}
