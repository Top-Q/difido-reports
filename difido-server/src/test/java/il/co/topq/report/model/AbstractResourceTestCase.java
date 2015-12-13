package il.co.topq.report.model;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.MainClass;
import il.co.topq.report.controller.resource.DifidoClient;
import il.co.topq.report.model.ExecutionManager.ExecutionMetaData;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractResourceTestCase {

	protected static final String HOST = "localhost";
	protected static final int PORT = 8080;

	private HttpServer server;
	protected DifidoClient difidoClient;

	private boolean sameVm = true;

	@Before
	public void setUp() throws Exception {
		if (sameVm) { 
			server = MainClass.startServer();
			MainClass.startElastic();
			MainClass.configureElastic();
			flushServer();
		}
		final String baseUri = Configuration.INSTANCE.readString(ConfigProps.BASE_URI);
		System.out.println("@Before - Grizzly server started on: " + baseUri);
		difidoClient = new DifidoClient(HOST, PORT);
	}

	private void flushServer() {
		try {
			FileUtils.deleteDirectory(new File("docRoot/reports"));
		} catch (IOException e) {
		}
		ExecutionManager.INSTANCE.executionsCache = new HashMap<Integer, ExecutionMetaData>();
	}

	@After
	public void tearDown() {
		if (sameVm) {
			flushServer();
			MainClass.stopElastic();
			server.shutdownNow();
		}
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
		final File[] executionFolders = new File("docRoot/reports").listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isDirectory() && file.getName().startsWith("exec")) {
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
