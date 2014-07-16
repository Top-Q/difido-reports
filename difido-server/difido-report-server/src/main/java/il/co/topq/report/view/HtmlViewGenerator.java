package il.co.topq.report.view;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.model.Session;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class HtmlViewGenerator implements ResourceChangedListener {

	private static final Logger log = Logger.getLogger(HtmlViewGenerator.class.getSimpleName());
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("HH_mm_ss_SS");

	private File executionDestinationFolder;

	enum HtmlGenerationLevel {
		EXECUTION, MACHINE, SCENARIO, TEST, TEST_DETAILS, ELEMENT
	}

	private HtmlGenerationLevel creationLevel = HtmlGenerationLevel.ELEMENT;

	@Override
	public void executionAdded(Execution execution) {
		prepareExecutionFolder();
		if (creationLevel.ordinal() >= HtmlGenerationLevel.EXECUTION.ordinal()) {
			writeExecution();
		}

	}

	private void prepareExecutionFolder() {
		final String executionFolderName = "execution_" + DATE_FORMATTER.format(new Date());
		executionDestinationFolder = new File(Configuration.INSTANCE.read(ConfigProps.REPORT_DESTINATION_FOLDER),
				executionFolderName);
		if (!executionDestinationFolder.exists()) {
			if (!executionDestinationFolder.mkdirs()) {
				String errorMessage = "Failed creating report destination folder in "
						+ executionDestinationFolder.getAbsolutePath();
				log.severe(errorMessage);
				// TODO: Handle errors
				return;
			}
		}
		PersistenceUtils.copyResources(executionDestinationFolder);

	}

	@Override
	public void machineAdded(MachineNode machine) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.MACHINE.ordinal()) {
			writeExecution();
		}

	}

	private void writeExecution() {
		PersistenceUtils.writeExecution(Session.INSTANCE.getExecution(), executionDestinationFolder);

	}

	private void writeTestDetails(TestNode test, TestDetails details) {
		PersistenceUtils.writeTest(details, executionDestinationFolder, new File(executionDestinationFolder, "tests"
				+ File.separator + "test_" + test.getIndex()));
	}

	@Override
	public void scenarioAdded(ScenarioNode scenario) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.SCENARIO.ordinal()) {
			writeExecution();
		}

	}

	@Override
	public void testAdded(TestNode test) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST.ordinal()) {
			writeExecution();
		}

	}

	@Override
	public void testEnded(TestNode test) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST.ordinal()) {
			writeExecution();
		}

	}

	@Override
	public void testDetailsAdded(TestNode test, TestDetails details) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST_DETAILS.ordinal()) {
			writeTestDetails(test, details);
		}

	}

	@Override
	public void reportElementAdded(TestNode test, ReportElement element) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.ELEMENT.ordinal()) {
			writeTestDetails(test, element.getParent());
		}

	}

	@Override
	public void executionEnded(Execution execution) {
		writeExecution();

	}

}
