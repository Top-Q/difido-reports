package il.co.topq.report.business.execution;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

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
	private MetadataController executionManager;

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
		try {
			FileUtils.deleteDirectory(reportsFolder);

		} catch (IOException e) {
			// This can happen. Let's give it another try
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
			FileUtils.deleteDirectory(reportsFolder);
		}
		executionManager.persistency.dump();
	}

	protected static Execution getExecution() {
		final File executionFolder = findSingleExecutionFolder();
		return PersistenceUtils.readExecution(executionFolder);
	}

	protected static Execution[] getAllExecutions() {
		final File[] executionFolders = findAllExecutionFolders();
		Execution[] executions = new Execution[executionFolders.length];
		for (int i = 0; i < executions.length; i++) {
			executions[i] = PersistenceUtils.readExecution(executionFolders[i]);
		}
		return executions;
	}

	protected static TestDetails getTestDetails(final String uid) {
		final File executionFolder = findSingleExecutionFolder();
		TestDetails test = PersistenceUtils.readTest(new File(executionFolder, "tests/test_" + uid));
		return test;
	}

	protected static File findSingleExecutionFolder() {
		final File[] executionFolders = findAllExecutionFolders();
		if (executionFolders.length > 0) {
			return executionFolders[0];
		}
		return null;
	}

	protected static File[] findFilesInTest( String uid, String fileName) {
		File testFolder = new File(findSingleExecutionFolder(), "tests/test_" + uid);
		return testFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (fileName.equals(name)) {
					return true;
				}
				return false;
			}

		});
	}

	protected static File[] findAllExecutionFolders() {
		final File[] executionFolders = new File("docRoot/reports").listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isDirectory() && file.getName().startsWith("exec")) {
					return true;
				}
				return false;
			}

		});
		return executionFolders;
	}

}
