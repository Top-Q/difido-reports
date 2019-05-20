package il.co.topq.report.updater;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UpgradeController {
	private static final Logger log = LoggerFactory.getLogger(UpgradeController.class);
	private static final String EXECUTION_FILE_NAME = "docRoot/reports/meta.json";

	private boolean parallel = true;

	public UpgradeController() {
		upgrade();
	}

	private void upgrade() {
		final File metaFile = new File(EXECUTION_FILE_NAME);
		if (!metaFile.exists()) {
			log.info("No need to upgrade persistency");
			return;
		}
		try {
			final JdbcTemplate template = JdbcTemplateBuilder.build();
			final MetaFileReader reader = new MetaFileReader(EXECUTION_FILE_NAME);
			final Map<Integer, OldMetadata> data = reader.read();
			final Worker worker = new Worker(template, data, parallel);
			long start = System.currentTimeMillis();
			worker.work();
			metaFile.renameTo(new File(EXECUTION_FILE_NAME + ".upgrade.backup"));
			log.info("Finished in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
		} catch (Exception e) {
			log.error("Failure in porting json to database", e);
		}

	}

}
