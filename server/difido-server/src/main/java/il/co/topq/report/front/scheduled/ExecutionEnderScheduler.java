package il.co.topq.report.front.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataProvider;
import il.co.topq.report.events.ExecutionEndedEvent;

@Component
public class ExecutionEnderScheduler {

	private final Logger log = LoggerFactory.getLogger(ExecutionEnderScheduler.class);

	private static int maxExecutionIdleTimeout = 0;

	private static boolean enabled;
	
	private final MetadataProvider metadataProvider;
	
	private final ApplicationEventPublisher publisher;

	@Autowired
	public ExecutionEnderScheduler(MetadataProvider metadataProvider, ApplicationEventPublisher publisher) {
		this.metadataProvider = metadataProvider;
		this.publisher = publisher;
	}

	static {
		maxExecutionIdleTimeout = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		if (maxExecutionIdleTimeout > 0) {
			enabled = true;
		}
	}

	@Scheduled(fixedRate = 30000)
	public void setExecutionsToNotActive() {
		if (!enabled) {
			log.debug("The executionEnder is disabled");
			return;
		}
		log.trace("Waking up in order to search for executions that need to end");
		final ExecutionMetadata[] metaDataArr = metadataProvider.getAllMetaData();
		for (ExecutionMetadata meta : metaDataArr) {
			if (!meta.isActive()) {
				continue;
			}
			final int idleTime = (int) (System.currentTimeMillis() - meta.getLastAccessedTime()) / 1000;
			if (null == meta.getExecution()) {
				log.warn("Active meta data of execution with id " + meta.getId() + " has no execution included");
			}
			if (idleTime > maxExecutionIdleTimeout) {
				log.debug("Execution with id " + meta.getId() + " idle time is " + idleTime
						+ " which exceeded the max idle time of " + maxExecutionIdleTimeout + ". Disabling execution");
				publisher.publishEvent(new ExecutionEndedEvent(meta));
			}
		}

	}

}
