package il.co.topq.report.business.html;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;
import il.co.topq.report.front.events.ExecutionCreatedEvent;
import il.co.topq.report.front.events.ExecutionDeletedEvent;
import il.co.topq.report.front.events.ExecutionEndedEvent;
import il.co.topq.report.front.events.FileAddedToTestEvent;
import il.co.topq.report.front.events.MachineCreatedEvent;
import il.co.topq.report.front.events.TestDetailsCreatedEvent;

@Component
public class HtmlViewGenerator {

	private final Logger log = LoggerFactory.getLogger(HtmlViewGenerator.class);

	private static final File TEMPLATE_FOLDER = new File("htmlTemplate");

	private Object executionFileLockObject = new Object();

	private Object testFileLockObject = new Object();

	enum HtmlGenerationLevel {
		EXECUTION, MACHINE, SCENARIO, TEST, TEST_DETAILS, ELEMENT
	}

	/**.
	 * TODO: Read from the configuration file
	 */
	private HtmlGenerationLevel creationLevel = HtmlGenerationLevel.ELEMENT;

	@EventListener
	public void onExecutionCreatedEvent(ExecutionCreatedEvent executionCreatedEvent) {
		prepareExecutionFolder(executionCreatedEvent.getMetadata());
		if (creationLevel.ordinal() >= HtmlGenerationLevel.EXECUTION.ordinal()) {
			writeExecution(executionCreatedEvent.getMetadata());
		}
	}

	/**
	 * This will delete the reports folder from the file system
	 */
	@EventListener
	public void onExecutionDeletedEvent(ExecutionDeletedEvent executionDeletedEvent) {
		log.debug("About to delete execution folder of execution with id " + executionDeletedEvent.getExecutionId());
		final File executionFolder = getExecutionDestinationFolder(executionDeletedEvent.getMetadata());
		if (null == executionFolder) {
			log.warn("Could not find folder for exeuction with id " + executionDeletedEvent.getExecutionId());
			return;
		}
		try {
			FileUtils.deleteDirectory(executionFolder);
		} catch (IOException e) {
			log.error("Failed to delete folder " + executionFolder.getAbsolutePath() + " for execution "
					+ executionDeletedEvent.getExecutionId());
		}
		log.debug("Finished deleting execution folder of execution with id " + executionDeletedEvent.getExecutionId());
	}

	private void prepareExecutionFolder(ExecutionMetadata executionMetadata) {
		synchronized (executionFileLockObject) {
			final File executionDestinationFolder = getExecutionDestinationFolder(executionMetadata);
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

	private File getExecutionDestinationFolder(ExecutionMetadata metadata) {
		if (null == metadata) {
			log.error("Failed to find execution metadata for execution with id ");
			return null;
		}
		final File executionDestinationFolder = new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER)
				+ File.separator + Common.REPORTS_FOLDER_NAME + File.separator + metadata.getFolderName());
		return executionDestinationFolder;
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.MACHINE.ordinal()) {
			writeExecution(machineCreatedEvent.getMetadata());
		}

	}

	private void writeExecution(ExecutionMetadata executionMetadata) {
		synchronized (executionFileLockObject) {
			PersistenceUtils.writeExecution(executionMetadata.getExecution(),
					getExecutionDestinationFolder(executionMetadata));

		}

	}

	private void writeTestDetails(TestDetails details, ExecutionMetadata executionMetadata) {
		synchronized (testFileLockObject) {
			final File executionDestinationFolder = getExecutionDestinationFolder(executionMetadata);
			PersistenceUtils.writeTest(details, executionDestinationFolder,
					new File(executionDestinationFolder, "tests" + File.separator + "test_" + details.getUid()));
		}
	}

	@EventListener
	public void onFileAddedToTestEvent(FileAddedToTestEvent fileAddedToTestEvent) {
		final File file = new File(
				getExecutionDestinationFolder(fileAddedToTestEvent.getMetadata()) + File.separator + "tests"
						+ File.separator + "test_" + fileAddedToTestEvent.getTestUid(),
				fileAddedToTestEvent.getFileName());

		try {
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
				stream.write(fileAddedToTestEvent.getFileContent());
			}
		} catch (IOException e) {
			log.warn("Failed to save file with name " + fileAddedToTestEvent.getFileName());
		}
	}

	@EventListener
	public void onTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST_DETAILS.ordinal()) {
			writeTestDetails(testDetailsCreatedEvent.getTestDetails(), testDetailsCreatedEvent.getMetadata());
		}

	}

	@EventListener
	public void executionEnded(ExecutionEndedEvent executionEndedEvent) {
		writeExecution(executionEndedEvent.getMetadata());

	}

}
