package il.co.topq.report.view;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.model.ExecutionManager;
import il.co.topq.report.model.ExecutionManager.ExecutionMetaData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlViewGenerator implements ResourceChangedListener {

	private final Logger log = LoggerFactory.getLogger(HtmlViewGenerator.class);

	private static HtmlViewGenerator INSTANCE = null;

	private static final File TEMPLATE_FOLDER = new File("htmlTemplate");

	private Object executionFileLockObject = new Object();

	private Object testFileLockObject = new Object();

	private HtmlViewGenerator() {
	}

	public static HtmlViewGenerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new HtmlViewGenerator();
		}
		return INSTANCE;
	}

	enum HtmlGenerationLevel {
		EXECUTION, MACHINE, SCENARIO, TEST, TEST_DETAILS, ELEMENT
	}

	/**
	 * TODO: Read from the configuration file
	 */
	private HtmlGenerationLevel creationLevel = HtmlGenerationLevel.ELEMENT;

	@Override
	public void executionAdded(int executionId, Execution execution) {
		prepareExecutionFolder(executionId);
		if (creationLevel.ordinal() >= HtmlGenerationLevel.EXECUTION.ordinal()) {
			writeExecution(executionId);
		}

	}

	private void prepareExecutionFolder(int executionId) {
		synchronized (executionFileLockObject) {
			final File executionDestinationFolder = getExecutionDestinationFolder(executionId);
			if (!TEMPLATE_FOLDER.exists() || !(new File(TEMPLATE_FOLDER, "index.html").exists())) {
				PersistenceUtils.copyResources(TEMPLATE_FOLDER);
			}

			if (!executionDestinationFolder.exists()) {
				if (!executionDestinationFolder.mkdirs()) {
					log.error("Failed creating report destination folder in "
							+ executionDestinationFolder.getAbsolutePath());
					return;
				}
			}
			try {
				FileUtils.copyDirectory(TEMPLATE_FOLDER, executionDestinationFolder);
			} catch (IOException e) {
				log.error("Failed copying html files to execution folder", e);
			}
		}

	}

	private File getExecutionDestinationFolder(int executionId) {
		final ExecutionMetaData executionMetaData = ExecutionManager.INSTANCE.getExecutionMetaData(executionId);
		if (null == executionMetaData) {
			log.error("Failed to find execution metadata for execution with id " + executionId);
			return null;
		}
		final File executionDestinationFolder = new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER)
				+ File.separator + Common.REPORTS_FOLDER_NAME + File.separator + executionMetaData.getFolderName());
		return executionDestinationFolder;
	}

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.MACHINE.ordinal()) {
			writeExecution(executionId);
		}

	}

	private void writeExecution(int executionId) {
		synchronized (executionFileLockObject) {
			PersistenceUtils.writeExecution(ExecutionManager.INSTANCE.getExecution(executionId),
					getExecutionDestinationFolder(executionId));

		}

	}

	private void writeTestDetails(TestDetails details, int executionId) {
		synchronized (testFileLockObject) {
			final File executionDestinationFolder = getExecutionDestinationFolder(executionId);
			PersistenceUtils.writeTest(details, executionDestinationFolder, new File(executionDestinationFolder,
					"tests" + File.separator + "test_" + details.getUid()));
		}
	}

	public void fileAddedToTest(int executionId, String uid, InputStream inputStream, String fileName) {
		final File file = new File(getExecutionDestinationFolder(executionId) + File.separator + "tests"
				+ File.separator + "test_" + uid, fileName);
		saveFile(inputStream, file.getAbsolutePath());
	}

	private void saveFile(InputStream inputStream, String filePath) {
		try {
			OutputStream outputStream = new FileOutputStream(filePath);
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();
		} catch (Exception e) {
			log.error("Failed to write file " + filePath, e);
		}
	}

	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST_DETAILS.ordinal()) {
			writeTestDetails(details, executionId);
		}

	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		writeExecution(executionId);

	}

}
