package il.co.topq.report.controller.resource;

import il.co.topq.difido.client.DifidoClient;
import il.co.topq.report.Main;
import il.co.topq.report.model.Session;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractResourceTestCase {

	private  HttpServer server;
	protected DifidoClient client;

	@Before
	public void setUp() {
		server = Main.startServer();
		System.out.println("@Before - Grizzly server started on: " + Main.BASE_URI);
		client = DifidoClient.build(Main.BASE_URI);
	}

	@After
	public void tearDown() {
		Session.INSTANCE.flush();
		server.shutdownNow();
		System.out.println("\n@After - Grizzly server shut down");
		System.out.println(Session.INSTANCE.getExecution());
	}

}
