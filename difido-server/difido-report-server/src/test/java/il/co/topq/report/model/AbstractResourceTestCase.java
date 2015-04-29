package il.co.topq.report.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.resource.DifidoClient;
import il.co.topq.report.model.Session.ExecutionMetaData;
import il.co.topq.report.MainClass;

import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractResourceTestCase {

	private HttpServer server;
	protected DifidoClient client;

	@Before
	public void setUp() throws Exception {
		server = MainClass.startServer();
		flushServer();
		final String baseUri = Configuration.INSTANCE.read(ConfigProps.BASE_URI);
		System.out.println("@Before - Grizzly server started on: " + baseUri);
	}

	private void flushServer() {
		try {
			FileUtils.deleteDirectory(new File("docRoot/reports"));
		} catch (IOException e) {
		}
		Session.INSTANCE.executions = new ArrayList<ExecutionMetaData>();
	}

	@After
	public void tearDown() {
		flushServer();
		server.shutdownNow();
		System.out.println("\n@After - Grizzly server shut down");
	}

	protected static Execution getExecution() {
		final File executionFolder = findExecutionFolder();
		return PersistenceUtils.readExecution(executionFolder);
	}

	protected static TestDetails getTestDetails(final String uid) {
		final File executionFolder = findExecutionFolder();
		TestDetails test = PersistenceUtils.readTest(new File(executionFolder, "tests/test_" + uid));
		return test;
	}

	protected static File findExecutionFolder() {
		final File[] executionFolders = new File("docRoot/reports").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("execution")) {
					return true;
				}
				return false;
			}

		});
		if (executionFolders.length > 0) {
			return executionFolders[0];
		}
		return null;
	}

}
