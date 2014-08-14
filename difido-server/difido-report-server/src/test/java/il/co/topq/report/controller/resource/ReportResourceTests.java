package il.co.topq.report.controller.resource;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.model.ExecutionReport;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import il.co.topq.report.controller.resource.AbstractResourceTestCase;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReportResourceTests extends AbstractResourceTestCase {

	private Client client;
	private WebTarget baseTarget;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		String baseUri = Configuration.INSTANCE.read(ConfigProps.BASE_URI);
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
		ExecutionReport[] reports = executionsTarget.request(MediaType.APPLICATION_JSON).get(ExecutionReport[].class);
		Assert.assertNotNull(reports);
		

	}
}
