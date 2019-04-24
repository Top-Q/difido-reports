package il.co.topq.report.front.scheduled;

import static il.co.topq.difido.DateTimeConverter.fromDateString;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionUpdatedEvent;
import il.co.topq.report.persistence.MetadataRepository;
@Component
public class HtmlReportsEraserScheduler {

	private final static Logger log = LoggerFactory.getLogger(HtmlReportsEraserScheduler.class);

	private int daysToKeep = 0;

	private boolean enabled;

	private final MetadataRepository metadataRepository;

	private final ApplicationEventPublisher publisher;

	@Autowired
	public HtmlReportsEraserScheduler(MetadataRepository metadataRepository, ApplicationEventPublisher publisher) {
		this.metadataRepository = metadataRepository;
		this.publisher = publisher;
		daysToKeep = Configuration.INSTANCE.readInt(ConfigProps.DAYS_TO_KEEP_HTML_REPORTS);
		if (daysToKeep > 0) {
			enabled = true;
		} else {
			log.debug("Html reports eraser is disabled");
		}
	}

	@Scheduled(fixedRate = 36_000_000)
	public void eraseOldHtmlReports() {
		if (!enabled) {
			return;
		}
		log.trace("Waking up in order to search for HTML reports that need to be erased");
		final LocalDate today = LocalDate.now();
		final List<ExecutionMetadata> metaDataArr = metadataRepository.findByActive(false);
		for (ExecutionMetadata meta : metaDataArr) {
			if (meta.isLocked() || !meta.isHtmlExists()) {
				continue;
			}
			final LocalDate executionDate = fromDateString(meta.getDate()).toLocalDate();
			final long old = ChronoUnit.DAYS.between(executionDate, today);

			if (old > daysToKeep) {
				log.debug("Execution with id " + meta.getId() + " creation date is " + meta.getDate()
						+ " which makes it " + old + " days old which is more then the maximum of " + old
						+ " to keep. About to delete HTML reports of the execution");
				meta.setHtmlExists(false);
				metadataRepository.save(meta);
				publisher.publishEvent(new ExecutionUpdatedEvent(meta.getId()));
			}

		}

	}

}