package il.co.topq.report.business.execution;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Application;
import il.co.topq.report.business.elastic.ESController;
import il.co.topq.report.business.execution.ExecutionManager;
import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;
import il.co.topq.report.front.rest.DifidoClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({ "server.port=8080" })
public abstract class AbstractResourceTest {

	private static final File reportsFolder = new File("docRoot/reports");

	@Value("${local.server.port}")
	private int port = 8080;

	protected URL base;

	protected DifidoClient client;
	
	@Autowired
	private ExecutionManager executionManager;
	
	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port + "/api/");
		ESController.enabled = false;
		client = new DifidoClient(base);
		flushPreviousReports();
	}
	
	@After
	public void tearDown() throws IOException {
		flushPreviousReports();
	}

	private void flushPreviousReports() throws IOException {
		FileUtils.deleteDirectory(reportsFolder);
		executionManager.executionsCache = new HashMap<Integer, ExecutionMetadata>();
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
