package il.co.topq.report.front.scheduled;

import static il.co.topq.difido.DateTimeConverter.fromDateString;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
import il.co.topq.report.events.ExecutionArchivedEvent;

@Component
public class HtmlReportsArchiver {
	private final static Logger log = LoggerFactory.getLogger(HtmlReportsArchiver.class);
	
	private int compressAfterXDays;

	private boolean enabled = false;

	private final MetadataProvider metadataProvider;

	private final ApplicationEventPublisher publisher;

	@Autowired
	public HtmlReportsArchiver(MetadataProvider metadataProvider, ApplicationEventPublisher publisher) {
		this.metadataProvider = metadataProvider;
		this.publisher = publisher;
		enabled = Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_COMPRESSED_RESOURCES);
		compressAfterXDays = Configuration.INSTANCE.readInt(ConfigProps.COMPRESS_AFTER_X_DAYS);
		if (!enabled) {
			log.debug("Html archive reports is disabled");
		}
	}
	@Scheduled(fixedRate = 3600000)
	public void archiveHtmlReports() {
		if (!enabled) {
			return;
		}
		
		log.trace("Waking up in order to search for HTML reports that need to be archived");
		final LocalDate today = LocalDate.now();
		final ExecutionMetadata[] metaDataArr = metadataProvider.getAllMetaData();
		for (ExecutionMetadata meta : metaDataArr) {
			if (meta.isActive() || meta.isLocked() || !meta.isHtmlExists() || meta.isArchived()) {
				continue;
			}
			final LocalDate executionDate = fromDateString(meta.getDate()).toLocalDate();
			final long old = ChronoUnit.DAYS.between(executionDate, today);

			if (old > compressAfterXDays) {
				log.debug("Execution with id " + meta.getId() + " creation date is " + meta.getDate()
						+ " which makes it " + old + " days old which is more then the maximum of " + compressAfterXDays
						+ " before archiving. About to archive HTML reports of the execution");
				publisher.publishEvent(new ExecutionArchivedEvent(meta));
			}

		}
		
		
	}

}
