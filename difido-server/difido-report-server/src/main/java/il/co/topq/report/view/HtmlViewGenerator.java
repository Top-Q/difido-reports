package il.co.topq.report.view;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.model.Session;

import java.io.File;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlViewGenerator implements ResourceChangedListener {

	private final Logger log = LoggerFactory.getLogger(HtmlViewGenerator.class);

	private static HtmlViewGenerator INSTANCE = null;

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

	private File executionDestinationFolder;

	enum HtmlGenerationLevel {
		EXECUTION, MACHINE, SCENARIO, TEST, TEST_DETAILS, ELEMENT
	}

	/**
	 * TODO: Read from the configuration file
	 */
	private HtmlGenerationLevel creationLevel = HtmlGenerationLevel.ELEMENT;

	@Override
	public void executionAdded(int executionId, Execution execution) {
		prepareExecutionFolder();
		if (creationLevel.ordinal() >= HtmlGenerationLevel.EXECUTION.ordinal()) {
			writeExecution(executionId);
		}

	}

	private void prepareExecutionFolder() {
		synchronized (executionFileLockObject) {
			final String executionFolderName = Common.EXECUTION_REPORT_FOLDER_PREFIX + "_"
					+ Common.EXECUTION_REPROT_TIMESTAMP_FORMATTER.format(new Date());
			executionDestinationFolder = new File(Configuration.INSTANCE.read(ConfigProps.DOC_ROOT_FOLDER)
					+ File.separator + Common.REPORTS_FOLDER_NAME, executionFolderName);
			if (!executionDestinationFolder.exists()) {
				if (!executionDestinationFolder.mkdirs()) {
					String errorMessage = "Failed creating report destination folder in "
							+ executionDestinationFolder.getAbsolutePath();
					log.error(errorMessage);
					return;
				}
			}
			PersistenceUtils.copyResources(executionDestinationFolder);
		}

	}

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.MACHINE.ordinal()) {
			writeExecution(executionId);
		}

	}

	private void writeExecution(int executionId) {
		synchronized (executionFileLockObject) {
			PersistenceUtils.writeExecution(Session.INSTANCE.getExecution(executionId), executionDestinationFolder);

		}

	}

	private void writeTestDetails(TestDetails details) {
		synchronized (testFileLockObject) {
			PersistenceUtils.writeTest(details, executionDestinationFolder, new File(executionDestinationFolder,
					"tests" + File.separator + "test_" + details.getUid()));
		}
	}


	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST_DETAILS.ordinal()) {
			writeTestDetails(details);
		}

	}



	@Override
	public void executionEnded(int executionId, Execution execution) {
		writeExecution(executionId);

	}

	public File getExecutionDestinationFolder() {
		return executionDestinationFolder;
	}
}
