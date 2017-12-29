package il.co.topq.report.business.execution;

import static il.co.topq.report.DateTimeConverter.fromDateObject;
import static il.co.topq.report.DateTimeConverter.fromElasticString;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.ExecutionUpdatedEvent;
import il.co.topq.report.events.FileAddedToTestEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;

@Component
public class MetadataController implements MetadataProvider, MetadataCreator {

	private final Logger log = LoggerFactory.getLogger(MetadataController.class);

	MetadataPersistency persistency;

	@Autowired
	public MetadataController(MetadataPersistency persistency) {
		this.persistency = persistency;
	}

	/**
	 * Adding new execution. Execution can have multiple machines that can have
	 * scenarios with tests.
	 * 
	 * @param executionDetails
	 *            The description of the execution.
	 * @return The execution metadata
	 */
	@Override
	public ExecutionMetadata createMetadata(ExecutionDetails executionDetails) {
		Execution execution = new Execution();
		final Date executionDate = new Date();
		final ExecutionMetadata metaData = new ExecutionMetadata(
				fromDateObject(executionDate).toElasticString(), execution);
		metaData.setTime(fromDateObject(executionDate).toTimeString());
		metaData.setDate(fromDateObject(executionDate).toDateString());
		metaData.setId(persistency.advanceId());
		metaData.setFolderName(Common.EXECUTION_REPORT_FOLDER_PREFIX + "_" + metaData.getId());
		metaData.setUri(Common.REPORTS_FOLDER_NAME + "/" + metaData.getFolderName() + "/index.html");
		metaData.setComment("");
		metaData.setActive(true);
		if (executionDetails != null) {
			metaData.setDescription(executionDetails.getDescription());
			metaData.setShared(executionDetails.isShared());
			setAllowedPropertiesToMetaData(metaData, executionDetails);
		}
		persistency.add(metaData);
		return metaData;
	}

	@EventListener
	public void onExecutionDeletionEvent(ExecutionDeletedEvent executionDeletedEvent) {
		deleteExecutionMetadata(executionDeletedEvent.getMetadata());
	}

	@EventListener
	public void onExecutionUpdatedEvent(ExecutionUpdatedEvent executionUpdatedEvent) {
		persistency.update(executionUpdatedEvent.getMetadata());

		// In some cases the update can be used only to trigger deletion of the
		// HTML reports and metadata.
		if (!executionUpdatedEvent.getMetadata().isHtmlExists() && !executionUpdatedEvent.getMetadata().isLocked()) {
			deleteExecutionMetadata(executionUpdatedEvent.getMetadata());
		}
	}

	private void deleteExecutionMetadata(ExecutionMetadata metadata) {
		log.debug("About to delete the metadata of execution with id " + metadata.getId());
		persistency.remove(metadata.getId());
		log.debug("Metadata of execution with id " + metadata.getId() + " was deleted");
	}

	/**
	 * Sets the execution properties in the execution meta data. Will allow
	 * addition only of properties that are specified in the configuration file
	 * 
	 * @param metaData
	 * @param executionDetails
	 */
	private void setAllowedPropertiesToMetaData(ExecutionMetadata metaData, ExecutionDetails executionDetails) {
		final List<String> allowedProperties = Configuration.INSTANCE.readList(ConfigProps.CUSTOM_EXECUTION_PROPERTIES);
		if (allowedProperties.isEmpty()) {
			metaData.setProperties(executionDetails.getExecutionProperties());
			return;
		}
		for (String executionProp : executionDetails.getExecutionProperties().keySet()) {
			if (allowedProperties.contains(executionProp)) {
				metaData.addProperty(executionProp, executionDetails.getExecutionProperties().get(executionProp));
			}
		}

	}

	@Override
	public ExecutionMetadata getMetadata(int executionId) {
		final ExecutionMetadata executionMetaData = persistency.get(executionId);
		if (null == executionMetaData) {
			log.error("Trying to get execution meta data of execution " + executionId + " which is not exist");
			return null;
		}
		if (executionMetaData.isActive()) {
			updateSingleExecutionMeta(executionId);
		}
		return new ExecutionMetadata(executionMetaData);
	}

	private void updateSingleExecutionMeta(int executionId) {
		ExecutionMetadata executionMetaData = persistency.get(executionId);
		updateDuration(executionMetaData);

		if (executionMetaData.getExecution() == null || executionMetaData.getExecution().getLastMachine() == null) {
			return;
		}
		int numOfTests = 0;
		int numOfSuccessfulTests = 0;
		int numOfFailedTests = 0;
		int numOfTestsWithWarnings = 0;
		int numOfMachines = 0;

		for (MachineNode machine : executionMetaData.getExecution().getMachines()) {
			numOfMachines++;
			final List<ScenarioNode> scenarios = machine.getChildren();
			if (null == scenarios) {
				continue;
			}
			for (ScenarioNode scenario : scenarios) {
				for (Node node : scenario.getChildren(true)) {
					if (node instanceof TestNode) {
						numOfTests++;
						switch (node.getStatus()) {
						case success:
							numOfSuccessfulTests++;
							break;
						case error:
						case failure:
							numOfFailedTests++;
							break;
						case warning:
							numOfTestsWithWarnings++;
						default:
							break;
						}
					}
				}
			}
		}
		synchronized (executionMetaData) {
			executionMetaData.setNumOfTests(numOfTests);
			executionMetaData.setNumOfFailedTests(numOfFailedTests);
			executionMetaData.setNumOfSuccessfulTests(numOfSuccessfulTests);
			executionMetaData.setNumOfTestsWithWarnings(numOfTestsWithWarnings);
			executionMetaData.setNumOfMachines(numOfMachines);
		}
		persistency.update(executionMetaData);

	}

	/**
	 * Updates the duration of the execution according to the start time.
	 *  
	 * @param executionMetaData
	 */
	private synchronized void updateDuration(final ExecutionMetadata executionMetaData) {
		final String timestamp = executionMetaData.getTimestamp();
		try {
			if (!StringUtils.isEmpty(timestamp)) {
				final Date startTime = fromElasticString(timestamp).toDateObject();
				executionMetaData.setDuration(new Date().getTime() - startTime.getTime());
			}
		} catch (NumberFormatException e) {
			log.warn("Failed to parse start time of execution '" + timestamp + "' due to '" + e.getMessage() + "'", e);
		}
	}

	@Override
	public ExecutionMetadata[] getAllMetaData() {
		final List<ExecutionMetadata> result = persistency.getAll();
		for (ExecutionMetadata meta : result) {
			if (meta.isActive()) {
				updateSingleExecutionMeta(meta.getId());
			}

		}
		return result.toArray(new ExecutionMetadata[] {});
	}

	/**
	 * Get the first, active shared execution
	 * 
	 * @return shared execution metadata or null if none was found
	 */
	@Override
	public ExecutionMetadata getShared() {
		for (ExecutionMetadata meta : persistency.getAll()) {
			if (meta.isActive() && meta.isShared()) {
				return meta;
			}
		}
		return null;

	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		final ExecutionMetadata metadata = persistency.get(executionEndedEvent.getExecutionId());
		if (null == metadata) {
			log.error("Trying to disable execution with id " + executionEndedEvent.getExecutionId()
					+ " which is not exist");
		}
		updateSingleExecutionMeta(metadata.getId());
		metadata.setActive(false);
		persistency.update(metadata);
	}

	private void updateExecutionLastUpdateTime(int executionId) {
		final ExecutionMetadata metadata = persistency.get(executionId);
		if (null == metadata) {
			log.error("Trying to update machine in execution with id " + executionId + " which is not exist");
		}
		metadata.setLastAccessedTime(System.currentTimeMillis());
		// We do not need to update the persistence. It is important only on
		// object level
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		updateExecutionLastUpdateTime(machineCreatedEvent.getExecutionId());
		updateAllExecutionsMetaData();
	}

	/**
	 * Updates the state of all the active executions.
	 */
	private void updateAllExecutionsMetaData() {
		for (ExecutionMetadata meta : persistency.getAll()) {
			if (meta.isActive()) {
				updateSingleExecutionMeta(meta.getId());
			}

		}
	}

	@EventListener
	public void onTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		updateExecutionLastUpdateTime(testDetailsCreatedEvent.getExecutionId());
	}

	@EventListener
	public void onFileAddedToTestEvent(FileAddedToTestEvent fileAddedToTestEvent) {
		updateExecutionLastUpdateTime(fileAddedToTestEvent.getExecutionId());

	}

}
