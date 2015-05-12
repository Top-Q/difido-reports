package il.co.topq.report.controller.resource;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.model.AbstractResourceTestCase;
import il.co.topq.report.model.ExecutionManager.ExecutionMetaData;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestReportResource extends AbstractResourceTestCase {

	private Client client;
	private WebTarget baseTarget;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		String baseUri = Configuration.INSTANCE.readString(ConfigProps.BASE_URI);
		client = ClientBuilder.newClient();
		client.register(JacksonFeature.class);
		client.register(MultiPartWriter.class);
		this.baseTarget = client.target(baseUri);
		client.target(baseUri);

	}

	@After
	public void tearDown() {
		if (client != null) {
			client.close();
		}
	}

	@Test
	public void testGetReports() {
		WebTarget executionsTarget = baseTarget.path("/reports");
		ExecutionMetaData[] reports = executionsTarget.request(MediaType.APPLICATION_JSON).get(ExecutionMetaData[].class);
		Assert.assertNotNull(reports);
		

	}
}
