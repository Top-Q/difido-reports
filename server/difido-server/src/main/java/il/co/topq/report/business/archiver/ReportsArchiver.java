package il.co.topq.report.business.archiver;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import il.co.topq.difido.DateTimeConverter;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataPersistency;

@Component
public class ReportsArchiver implements Archiver {

	private final Logger log = LoggerFactory.getLogger(ReportsArchiver.class);

	/**
	 * The local persistence
	 */
	private MetadataPersistency persistency;

	/**
	 * Is this service enabled or not. Can be disabled from the configuration or
	 * as a result of malfunctioning.
	 */
	private boolean enabled = true;

	/**
	 * The minimum required age of execution in order for it to be archived
	 */
	private final long minReportsAgeInMillis;

	/**
	 * The client that is used for connecting with the remote Difido server
	 */
	private final ArchiverHttpClient client;

	/**
	 * The location of the local executions folder
	 */
	private final String reportsFolder;

	/**
	 * Provides the concurrency mechanisms
	 */
	@Autowired
	@Qualifier(value = "archiveExecutor")
	ThreadPoolTaskExecutor executor;

	@Autowired
	public ReportsArchiver(MetadataPersistency persistency) {
		this.persistency = persistency;
		client = new ArchiverHttpClient(Configuration.INSTANCE.readString(ConfigProps.ARCHIVER_DIFIDO_SERVER));
		enabled = Configuration.INSTANCE.readBoolean(ConfigProps.ARCHIVER_ENABLED);
		final int minReportsAge = Configuration.INSTANCE.readInt(ConfigProps.ARCHIVER_MIN_REPORTS_AGE);
		minReportsAgeInMillis = TimeUnit.DAYS.toMillis(minReportsAge);
		reportsFolder = Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER) + File.separator
				+ Common.REPORTS_FOLDER_NAME;
	}

	/**
	 * Starting the archive process. <br>
	 * 1. Initialize the local reports folder if it is not exists.<br>
	 * 2. Get all the remote executions<br>
	 * 3. Archive old and finished executions <br>
	 * 
	 */
	@Override
	public void archive() {
		if (!enabled) {
			return;
		}
		initReportsFolder();
		final Map<Integer, ExecutionMetadata> remoteExecutions = getAllRemoteExecutions();
		final List<ExecutionMetadata> executionsToArchive = filterExecutionsToArchive(remoteExecutions);
		archiveExecutions(executionsToArchive);
	}

	/**
	 * Creates new report folder if none exists
	 */
	private void initReportsFolder() {
		if (!new File(reportsFolder).exists()) {
			log.debug("Preperaring new " + reportsFolder + " folder");
			if (!new File(reportsFolder).mkdirs()) {
				log.debug("Failed to create " + reportsFolder + " folder. Disabling archiver");
				enabled = false;
			}
		}
	}

	/**
	 * Archives the executions that are old enough, finished and are not in the
	 * local server
	 * 
	 * @param executionsToArchive
	 */
	private void archiveExecutions(final List<ExecutionMetadata> executionsToArchive) {
		executor.submit(() -> executionsToArchive.parallelStream().forEach(e -> {
			final File archivedHtmlFile = getArchivedHtmlFile(e);
			if (null == archivedHtmlFile) {
				return;
			}
			try {
				extractFilesToDestination(archivedHtmlFile);
				if (!checkReportsIntegrity(e.getId())) {
					log.error("Retreiving HTML file for execution " + e.getId()
							+ " was unsuccssful. Rolling back and deleting local execution");
					deleteExecutionFolder(e.getId());
					return;
				}
				addExecutionToPersistency(e);
				deleteRemoteExecution(e);
			} catch (ExecutionException e1) {
				log.error("Error extracting file " + archivedHtmlFile.getName() + "error :" + e1.getMessage());
			} finally {
				archivedHtmlFile.delete();
			}
		}));
	}

	/**
	 * Deletes the local execution folder.
	 * 
	 * @param id
	 *            Id of execution to be deleted
	 */
	private void deleteExecutionFolder(int id) {
		log.debug("Deleting execution folder from local Difido");
		File folderToDelete = new File(reportsFolder, Common.EXECUTION_REPORT_FOLDER_PREFIX + "_" + id);
		try {
			FileUtils.deleteDirectory(folderToDelete);
		} catch (IOException e) {
			log.error("Failed to delete folder " + folderToDelete.getAbsolutePath(), e);
		}
	}

	/**
	 * Checks if the remote execution folder and the local execution folder are
	 * identical
	 * 
	 * @param id
	 *            Execution id
	 * @return True of both execution folders are identical
	 */
	private boolean checkReportsIntegrity(int id) {
		final String response = client.getString("/api/reports/" + id + "/size");
		if (null == response || response.isEmpty()) {
			log.error("Failed to get response from remote Difido about size of execution");
		}
		final long remoteSize = Long.parseLong(response);
		if (remoteSize <= 0) {
			log.error("Failed to get remote HTML folder size. Recieved " + remoteSize);
			return false;
		}
		log.debug("Remote execution folder size is " + remoteSize + " bytes");
		final long localSize = FileUtils
				.sizeOfDirectory(new File(reportsFolder, Common.EXECUTION_REPORT_FOLDER_PREFIX + "_" + id));
		if (localSize != remoteSize) {
			log.error("Local execution folder size (" + localSize + ") of execution " + id
					+ " is not equals to remote size (" + remoteSize + ")");
			return false;
		}
		return true;
	}

	/**
	 * Get the execution files as ZIP file from the remote server
	 * 
	 * @param execution
	 * @return The reports as ZIP file
	 */
	private File getArchivedHtmlFile(ExecutionMetadata execution) {
		final File archivedHtmlFile = client.getFile("/api/reports/" + execution.getId(),
				Common.EXECUTION_REPORT_FOLDER_PREFIX + "_" + execution.getId() + ".zip");
		if (null == archivedHtmlFile) {
			log.error("Failed to get execution zip file for execution " + execution.getId());
		}
		log.debug("Got archived HTML file " + archivedHtmlFile.getName());
		return archivedHtmlFile;
	}

	/**
	 * Will add the execution to the persistency.
	 * 
	 * @param execution
	 */
	private void addExecutionToPersistency(ExecutionMetadata execution) {
		log.debug("Adding execution " + execution.getId() + " to persistency");
		execution.setDirty(true);
		persistency.add(execution);
	}

	private void deleteRemoteExecution(ExecutionMetadata e) {
		if (Configuration.INSTANCE.readBoolean(ConfigProps.ARCHIVER_DELETE_AFTER_ARCHIVE)) {
			log.debug("About to delete execution " + e.getId() + " from main Difido server");
			client.delete("/api/executions/" + e.getId());
		}
	}

	/**
	 * Get the list of all the remote executions and filter it according to if
	 * the executions are closed, old enough and not exists in the local server
	 * 
	 * @param remoteExecutions
	 * @return Executions to archive
	 */
	private List<ExecutionMetadata> filterExecutionsToArchive(Map<Integer, ExecutionMetadata> remoteExecutions) {
		final List<ExecutionMetadata> executionsToArchive = remoteExecutions.values().parallelStream()
				.filter(e -> !e.isActive())
				.filter(el -> persistency.getAll().stream().noneMatch(er -> er.getId() == el.getId()))
				.filter(e -> new Date().getTime() - DateTimeConverter.fromDateString(e.getDate()).toDateObject()
						.getTime() > minReportsAgeInMillis)
				.collect(Collectors.toList());
		log.debug("There are " + executionsToArchive.size() + " that needs to be archived in the remote Difido server");
		return executionsToArchive;
	}

	/**
	 * Get all the remote executions not filtered
	 * 
	 * @return Map of ids to execution metadata
	 */
	private Map<Integer, ExecutionMetadata> getAllRemoteExecutions() {
		Map<Integer, ExecutionMetadata> remoteExecutions = client.get("/" + Common.REPORTS_FOLDER_NAME + "/meta.json",
				new TypeReference<Map<Integer, ExecutionMetadata>>() {
				});
		log.debug("Found " + remoteExecutions.size() + " execution in Difido server");
		return remoteExecutions;
	}

	/**
	 * Extracts the file to the local folder
	 * 
	 * @param archiveFile
	 *            File to extract
	 * @throws ExecutionException
	 */
	private void extractFilesToDestination(File archiveFile) throws ExecutionException {
		UnzipUtility unZipper = new UnzipUtility();
		try {
			unZipper.unzip(archiveFile.getPath(), reportsFolder);
		} catch (IOException e) {
			log.error("Couldn't extract the file " + archiveFile.getAbsolutePath(), e);
		}
	}
}
