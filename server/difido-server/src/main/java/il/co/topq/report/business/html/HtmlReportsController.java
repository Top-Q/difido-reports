package il.co.topq.report.business.html;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.StopWatch;
import il.co.topq.report.business.AsyncActionQueue;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionCreatedEvent;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.ExecutionUpdatedEvent;
import il.co.topq.report.events.FileAddedToTestEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;
import il.co.topq.report.persistence.ExecutionRepository;
import il.co.topq.report.persistence.ExecutionState;
import il.co.topq.report.persistence.ExecutionStateRepository;
import il.co.topq.report.persistence.MetadataRepository;

@Component
public class HtmlReportsController {

	private final Logger log = LoggerFactory.getLogger(HtmlReportsController.class);

	private static final File TEMPLATE_FOLDER = new File("htmlTemplate");

	private AsyncActionQueue queue;

	private final ExecutionRepository executionRepository;

	private final MetadataRepository metadataRepository;

	private final ExecutionStateRepository stateRepository;

	enum HtmlGenerationLevel {
		EXECUTION, MACHINE, SCENARIO, TEST, TEST_DETAILS, ELEMENT
	}

	@Autowired
	public HtmlReportsController(AsyncActionQueue queue, ExecutionRepository executionRepository,
			ExecutionStateRepository stateRepository, MetadataRepository metadataRepository) {
		this.queue = queue;
		this.executionRepository = executionRepository;
		this.metadataRepository = metadataRepository;
		this.stateRepository = stateRepository;
	}

	/**
	 * . TODO: Read from the configuration file
	 */
	private HtmlGenerationLevel creationLevel = HtmlGenerationLevel.ELEMENT;

	@EventListener
	public void onExecutionCreatedEvent(ExecutionCreatedEvent executionCreatedEvent) {
		final ExecutionMetadata metadata = metadataRepository.findById(executionCreatedEvent.getExecutionId());
		prepareExecutionFolder(metadata);
		if (creationLevel.ordinal() >= HtmlGenerationLevel.EXECUTION.ordinal()) {
			writeExecution(metadata);
		}
	}

	/**
	 * This will delete the reports folder from the file system
	 *
	 * @param executionDeletedEvent
	 * 		Event that is called when execution is deleted
	 *
	 */
	@EventListener
	public void onExecutionDeletedEvent(ExecutionDeletedEvent executionDeletedEvent) {
		final ExecutionMetadata metadata = metadataRepository.findById(executionDeletedEvent.getExecutionId());
		deleteHtmlFolder(metadata);
	}

	/**
	 * This will be triggered only when an update event is send and the HTML is
	 * marked as false in the metadata. <br>
	 * In this case, the HTML will be deleted.
	 * 
	 * 
	 * @param executionUpdatedEvent
	 * 		Event that is called when execution is updated
	 */
	@EventListener
	public void onExecutionUpdatedEvent(ExecutionUpdatedEvent executionUpdatedEvent) {
		final ExecutionState state = stateRepository.getOne(executionUpdatedEvent.getExecutionId());
		if (state.isHtmlExists() || state.isLocked()) {
			return;
		}
		final ExecutionMetadata metadata = metadataRepository.findById(executionUpdatedEvent.getExecutionId());
		final File executionFolder = getExecutionDestinationFolder(metadata);
		if (executionFolder != null && executionFolder.exists()) {
			deleteHtmlFolder(metadata);
		}
	}

	/**
	 * Delete the HTML folder of the given execution
	 * 
	 * @param executionMetadata
	 */
	private void deleteHtmlFolder(ExecutionMetadata executionMetadata) {
		log.debug("About to delete execution folder of execution with id " + executionMetadata.getId());
		final File executionFolder = getExecutionDestinationFolder(executionMetadata);
		if (null == executionFolder) {
			log.warn("Could not find folder for execution with id " + executionMetadata.getId());
			return;
		}
		queue.addAction("Delete execution " + executionMetadata.getId() + " from disk", () -> {
			try {
				FileUtils.deleteDirectory(executionFolder);
				log.debug("Finished deleting execution folder of execution with id " + executionMetadata.getId());
			} catch (IOException e) {
				log.warn("Failed to delete folder " + executionFolder.getAbsolutePath() + " for execution "
						+ executionMetadata.getId());
			}
		});
	}

	private void prepareExecutionFolder(ExecutionMetadata executionMetadata) {
		final File executionDestinationFolder = getExecutionDestinationFolder(executionMetadata);
		queue.addAction("Prepare execution folder for execution " + executionMetadata.getId(), () -> {
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
		});
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
		final ExecutionMetadata metadata = metadataRepository.findById(machineCreatedEvent.getExecutionId());
		if (creationLevel.ordinal() >= HtmlGenerationLevel.MACHINE.ordinal()) {
			writeExecution(metadata);
		}

	}

	private void writeExecution(ExecutionMetadata executionMetadata) {
		queue.addAction("Write execution " + executionMetadata.getId() + " to disk", () -> {
			StopWatch stopWatch = new StopWatch(log).start("Writing execution " + executionMetadata.getId());
			final Execution execution = executionRepository.findById(executionMetadata.getId());
			PersistenceUtils.writeExecution(execution, getExecutionDestinationFolder(executionMetadata));
			stopWatch.stopAndLog();
		});
	}

	private void writeTestDetails(TestDetails details, ExecutionMetadata executionMetadata) {
		queue.addAction("Write test details of execution " + executionMetadata.getId() + " to disk", () -> {
			StopWatch stopWatch = new StopWatch(log).start(
					"Writing test details of test " + details.getUid() + " for execution " + executionMetadata.getId());
			final File executionDestinationFolder = getExecutionDestinationFolder(executionMetadata);
			PersistenceUtils.writeTest(details, executionDestinationFolder,
					buildTestFolderName(executionDestinationFolder, details.getUid()));
			stopWatch.stopAndLog();
		});
	}

	@EventListener
	public void onFileAddedToTestEvent(FileAddedToTestEvent fileAddedToTestEvent) {
		StopWatch stopWatch = new StopWatch(log).start("Writing file " + fileAddedToTestEvent.getFileName()
				+ " for test " + fileAddedToTestEvent.getTestUid());
		final ExecutionMetadata metadata = metadataRepository.findById(fileAddedToTestEvent.getExecutionId());
		final File destinationFolder = buildTestFolderName(getExecutionDestinationFolder(metadata),
				fileAddedToTestEvent.getTestUid());
		queue.addAction("Write file " + fileAddedToTestEvent.getFileName() + " of execution "
				+ fileAddedToTestEvent.getExecutionId() + " to disk",() -> {
			if (!destinationFolder.exists()) {
				// In some cases there can be a race condition between the
				// WriteTestDetails method and this method. If this method will
				// be
				// executed first, the destination folder will not be ready so
				// we
				// have to create it here
				try {
					FileUtils.forceMkdir(destinationFolder);
				} catch (IOException e) {
					log.warn("Test destination folder '" + destinationFolder.getAbsolutePath()
							+ "'is not exist and failed to create one for storing file '"
							+ fileAddedToTestEvent.getFileName() + "'");
					return;
				}
			}

			final File file = new File(destinationFolder, fileAddedToTestEvent.getFileName());
			try {
				if (!file.createNewFile()) {
					log.warn("Failed to create new file " + file.getAbsolutePath()
							+ ", probably because file with the same name is already exists in the folder "
							+ destinationFolder);
					return;
				}
			} catch (IOException e) {
				log.warn("Failed to create new file " + file.getAbsolutePath() + " due to " + e.getMessage());
				return;
			}

			try {
				try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
					stream.write(fileAddedToTestEvent.getFileContent());
				}
			} catch (IOException e) {
				log.warn("Failed to save file with name " + fileAddedToTestEvent.getFileName() + " due to "
						+ e.getMessage());
			}
			stopWatch.stopAndLog();
		});
	}

	@EventListener
	public void onTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		final ExecutionMetadata metadata = metadataRepository.findById(testDetailsCreatedEvent.getExecutionId());
		if (creationLevel.ordinal() >= HtmlGenerationLevel.TEST_DETAILS.ordinal()) {
			writeTestDetails(testDetailsCreatedEvent.getTestDetails(), metadata);
		}

	}

	@EventListener
	public void executionEnded(ExecutionEndedEvent executionEndedEvent) {
		final ExecutionMetadata metadata = metadataRepository.findById(executionEndedEvent.getExecutionId());
		writeExecution(metadata);

	}

	private static File buildTestFolderName(final File executionDestinationFolder, String uid) {
		return new File(executionDestinationFolder, "tests" + File.separator + "test_" + uid);
	}

}