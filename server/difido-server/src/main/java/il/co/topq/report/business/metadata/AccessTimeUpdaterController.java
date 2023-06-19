package il.co.topq.report.business.metadata;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.events.ExecutionCreatedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.FileAddedToTestEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;

@Component
public class AccessTimeUpdaterController implements InfoContributor {

	private final static Logger log = LoggerFactory.getLogger(AccessTimeUpdaterController.class);

	private Map<Integer, Long> accessTimePerExecution;

	private boolean enabled;

	@Autowired
	public AccessTimeUpdaterController() {
		super();
		if (Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC) > 0) {
			enabled = true;
			accessTimePerExecution = new HashMap<>();
		} else {
			log.debug("Html reports eraser is disabled");
		}
	}

	@EventListener
	public void onExecutionCreatedEvent(ExecutionCreatedEvent executionCreatedEvent) {
		updateExecutionLastUpdateTime(executionCreatedEvent.getExecutionId());
	}
	
	@EventListener
	public void onTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {
		updateExecutionLastUpdateTime(testDetailsCreatedEvent.getExecutionId());
	}
	
	@EventListener
	public void onFileAddedToTestEvent(FileAddedToTestEvent fileAddedToTestEvent) {
		updateExecutionLastUpdateTime(fileAddedToTestEvent.getExecutionId());
	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		deleteAccessTime(executionEndedEvent.getExecutionId());
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		updateExecutionLastUpdateTime(machineCreatedEvent.getExecutionId());
	}

	private void updateExecutionLastUpdateTime(int id) {
		if (!enabled) {
			return;
		}
		synchronized (accessTimePerExecution) {
			accessTimePerExecution.put(id, System.currentTimeMillis());
		}
	}

	private void deleteAccessTime(int executionId) {
		if (!enabled) {
			return;
		}
		synchronized (accessTimePerExecution) {
			accessTimePerExecution.remove(executionId);
		}
	}

	public long getLastAccessTime(int executionId) {
		if (!enabled) {
			return 0;
		}
		if (null == accessTimePerExecution) {
			return 0;
		}
		if (null == accessTimePerExecution.get(executionId)) {
			return 0;
		}
		return accessTimePerExecution.get(executionId);
	}

	/**
	 * Info about the server that can be retrieved using the
	 * http://[host]:[port]/info request
	 */
	@Override
	public void contribute(Builder builder) {
		final Map<String, Integer> metadataDetails = new HashMap<>();
		metadataDetails.put("enabled", enabled ? 1 : 0);
		if (enabled) {
			metadataDetails.put("active executions", (int) accessTimePerExecution.size());
		}
		builder.withDetail("access time updater controller", metadataDetails);
	}

}
