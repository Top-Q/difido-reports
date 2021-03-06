package il.co.topq.report.front.scheduled;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.metadata.AccessTimeUpdaterController;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.persistence.ExecutionState;
import il.co.topq.report.persistence.ExecutionStateRepository;

@Component
public class ExecutionEnderScheduler {

	private final Logger log = LoggerFactory.getLogger(ExecutionEnderScheduler.class);

	private static int maxExecutionIdleTimeout = 0;

	private static boolean enabled;

	private final ApplicationEventPublisher publisher;

	private final ExecutionStateRepository stateRepository;

	private final AccessTimeUpdaterController accessTimeUpdater;

	@Autowired
	public ExecutionEnderScheduler(ExecutionStateRepository stateRepository,
			AccessTimeUpdaterController accessTimeUpdater, ApplicationEventPublisher publisher) {
		this.stateRepository = stateRepository;
		this.accessTimeUpdater = accessTimeUpdater;
		this.publisher = publisher;
	}

	static {
		maxExecutionIdleTimeout = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		if (maxExecutionIdleTimeout > 0) {
			enabled = true;
		}
	}

	@PostConstruct
	public void closeAllExecutions() {
		log.debug("Closing all active execution");
		List<ExecutionState> activeStates = stateRepository.findByActive(true);
		if (activeStates.isEmpty()) {
			return;
		}
		log.info("About to close " + activeStates.size() + " executions that were left active from last run");
		activeStates.stream().forEach(s -> s.setActive(false));
		activeStates.forEach(s -> stateRepository.save(s));
	}

	@Scheduled(fixedRate = 30000)
	public void setExecutionsToNotActive() {
		if (!enabled) {
			log.debug("The executionEnder is disabled");
			return;
		}
		log.trace("Waking up in order to search for executions that need to end");
		final List<ExecutionState> stateList = stateRepository.findByActive(true);
		for (ExecutionState state : stateList) {
			long lastAccessTime = accessTimeUpdater.getLastAccessTime(state.getId());
			if (lastAccessTime == 0) {
				// No records were done for this execution
				continue;
			}
			final int idleTime = (int) (System.currentTimeMillis() - lastAccessTime) / 1000;
			if (idleTime > maxExecutionIdleTimeout) {
				log.debug("Execution with id " + state.getId() + " idle time is " + idleTime
						+ " which exceeded the max idle time of " + maxExecutionIdleTimeout + ". Disabling execution");
				state.setActive(false);
				stateRepository.save(state);
				publisher.publishEvent(new ExecutionEndedEvent(state.getId()));
			}
		}

	}

}
