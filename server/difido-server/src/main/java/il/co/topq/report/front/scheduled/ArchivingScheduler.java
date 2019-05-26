package il.co.topq.report.front.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.archive.Archiver;

@Component
public class ArchivingScheduler {

	private final static Logger log = LoggerFactory.getLogger(ArchivingScheduler.class);

	private boolean enabled;

	@Autowired
	private Archiver archiver;

	public ArchivingScheduler() {
		enabled = Configuration.INSTANCE.readBoolean(ConfigProps.ARCHIVER_ENABLED);
		log.info("Archiving service is " + (enabled ? "enabled" : "disabled"));
	}

	/**
	 * The archiver will wake up once in every 6 hours
	 */
	@Scheduled(fixedRate = 21_600_000)
	public void setExecutionsToNotActive() {
		if (!enabled) {
			return;
		}
		log.debug("Starting archiving process");
		archiver.archive();
	}

}
