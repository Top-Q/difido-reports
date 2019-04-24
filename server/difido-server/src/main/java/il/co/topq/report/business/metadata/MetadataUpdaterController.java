package il.co.topq.report.business.metadata;

import static il.co.topq.difido.DateTimeConverter.fromElasticString;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.FileAddedToTestEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;
import il.co.topq.report.events.UpdateAllMetadatasRequestEvent;
import il.co.topq.report.events.UpdateMetadataRequestEvent;
import il.co.topq.report.persistence.ExecutionRepository;
import il.co.topq.report.persistence.MetadataRepository;

@Component
public class MetadataUpdaterController  implements InfoContributor {

	private final Logger log = LoggerFactory.getLogger(MetadataUpdaterController.class);
	
	private MetadataRepository metadataRepository;

	private ExecutionRepository executionRepository;

	@Autowired
	public MetadataUpdaterController(MetadataRepository metadataRepository, ExecutionRepository executionRepository) {
		this.metadataRepository = metadataRepository;
		this.executionRepository = executionRepository;
	}

	@EventListener
	public void onUpdateAllMetadatasRequestEvent(UpdateAllMetadatasRequestEvent updateAllMetadatasRequestEvent) {
		updateAllExecutionsMetaData();
	}
	
	@EventListener
	public void onUpdateMetadataRequestEvent(UpdateMetadataRequestEvent updateMetadataRequestEvent) {
		updateSingleExecutionMetaAndSave(updateMetadataRequestEvent.getExecutionId());
	}
	

	@EventListener
	public void onTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		final ExecutionMetadata metadata = metadataRepository.findById(testDetailsCreatedEvent.getExecutionId());
		updateExecutionLastUpdateTime(metadata);
		metadataRepository.save(metadata);
	}

	@EventListener
	public void onFileAddedToTestEvent(FileAddedToTestEvent fileAddedToTestEvent) {
		final ExecutionMetadata metadata = metadataRepository.findById(fileAddedToTestEvent.getExecutionId());
		updateExecutionLastUpdateTime(metadata);
		metadataRepository.save(metadata);
	}
	
	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		updateSingleExecutionMetaAndSave(executionEndedEvent.getExecutionId());
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		updateSingleExecutionMetaAndSave(machineCreatedEvent.getExecutionId());
	}
	
	private void updateSingleExecutionMetaAndSave(int executionId) {
		final ExecutionMetadata metadata = metadataRepository.findById(executionId);
		updateSingleExecutionMeta(metadata);
		metadataRepository.save(metadata);
		
	}
	
	private void updateExecutionLastUpdateTime(ExecutionMetadata metadata) {
		metadata.setLastAccessedTime(System.currentTimeMillis());
	}
	
	private void updateAllExecutionsMetaData() {
		for (ExecutionMetadata meta : metadataRepository.findByActive(true)) {
			updateSingleExecutionMeta(meta);
			metadataRepository.save(meta);

		}
	}

	private void updateSingleExecutionMeta(ExecutionMetadata executionMetaData) {
		updateDuration(executionMetaData);
		Execution execution = executionRepository.findById(executionMetaData.getId());

		if (execution == null || execution.getLastMachine() == null) {
			return;
		}
		int numOfTests = 0;
		int numOfSuccessfulTests = 0;
		int numOfFailedTests = 0;
		int numOfTestsWithWarnings = 0;
		int numOfMachines = 0;

		for (MachineNode machine : execution.getMachines()) {
			numOfMachines++;
			final List<ScenarioNode> scenarios = machine.getChildren();
			if (null == scenarios) {
				continue;
			}
			for (ScenarioNode scenario : scenarios) {
				for (Node node : scenario.getChildren(true)) {
					if (node instanceof TestNode) {
						switch (node.getStatus()) {
						case success:
							if (((TestNode) node).isHideInHtml()) {
								// It is important to put 'continue' here since
								// we don't want to get to the end of the 'for'
								// code block to the part that we add this test
								// to the number of tests. This supposed to be
								// completely hidden
								continue;
							}
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
						numOfTests++;
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
		updateExecutionLastUpdateTime(executionMetaData);

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
	
	/**
	 * Info about the server that can be retrieved using the
	 * http://<host>:<port>/info request
	 */
	@Override
	public void contribute(Builder builder) {
		Map<String, Integer> metadataDetails = new HashMap<>();
		metadataDetails.put("existing executions", (int) metadataRepository.count());
		metadataDetails.put("active executions", (int) metadataRepository.findByActive(true).size());
		builder.withDetail("metadata controller", metadataDetails);
	}

	
	
}
