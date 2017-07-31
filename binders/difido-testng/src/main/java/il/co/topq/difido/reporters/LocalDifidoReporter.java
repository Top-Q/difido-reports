package il.co.topq.difido.reporters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.ISuite;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.test.TestDetails;

public class LocalDifidoReporter extends AbstractDifidoReporter {

	private File reportDir;

	private File currentTestFolder;

	/**
	 * When files are added in the setup phase, there is no test context and no
	 * test folder that they can be copied to. In those cases we keep the files
	 * in list so we could copy it to the folder later on
	 */
	private List<File> bufferedFiles = new ArrayList<File>();

	@Override
	protected void writeTestDetails(TestDetails testDetails) {
		PersistenceUtils.writeTest(testDetails, new File(reportDir + File.separator + "current"), currentTestFolder);

	}

	@Override
	protected void writeExecution(Execution execution) {
		PersistenceUtils.writeExecution(execution, new File(reportDir + File.separator + "current"));

	}

	@Override
	public void onStart(ISuite suite) {
		super.onStart(suite);
		reportDir = new File(new File(suite.getOutputDirectory()).getParent(), "difido");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}
		final File currentLogFolder = new File(reportDir, "current");
		if (currentLogFolder.exists()) {
			try {
				FileUtils.deleteDirectory(currentLogFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final File templateFolder = new File(reportDir, "template");
		if (!templateFolder.exists() || !(new File(templateFolder, "index.html").exists())) {
			PersistenceUtils.copyResources(templateFolder);
		}
		final File testDetailsHtmlFile = new File(currentLogFolder, PersistenceUtils.TEST_DETAILS_HTML_FILE);
		final File indexFile = new File(currentLogFolder, "index.html");
		if (!testDetailsHtmlFile.exists() && !indexFile.exists()) {
			try {
				FileUtils.copyDirectory(templateFolder, currentLogFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void addFile(File file) {
		if (file == null || !file.exists() || !file.isFile()) {
			return;
		}
		if (isInSetup()) {
			bufferedFiles.add(file);
		} else {
			copyFileToTestFolder(file);
		}
	}

	private void copyFileToTestFolder(File file) {
		try {
			Files.copy(Paths.get(file.getAbsolutePath()),
					Paths.get(currentTestFolder + File.separator + file.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void updateTestDirectory() {
		currentTestFolder = new File(reportDir,
				"current" + File.separator + "tests" + File.separator + "test_" + getCurrentTest().getUid());
	}

	@Override
	public String getName() {
		return "DifidoLocalReporter";
	}

	@Override
	public File getCurrentTestFolder() {
		return currentTestFolder;
	}

	/**
	 * Elements that are created in setup phases, before test context is created
	 * are stored and flushed in the beginning of the test. <br>
	 * We use this to copy files that were also added in the setup phase to the
	 * test folder.
	 */
	@Override
	protected void flushBufferedElements(String elementsDescription) {
		super.flushBufferedElements(elementsDescription);
		if (!bufferedFiles.isEmpty()) {
			for (File file : bufferedFiles) {
				copyFileToTestFolder(file);
			}
			bufferedFiles.clear();
		}

	}

	@Override
	protected void onScenarioStart(ScenarioNode scenario) {
		// Unused
	}

}