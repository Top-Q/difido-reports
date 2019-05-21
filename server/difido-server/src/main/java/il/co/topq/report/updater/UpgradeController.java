package il.co.topq.report.updater;

import java.io.File;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;

@Component
public class UpgradeController {
	private static final Logger log = LoggerFactory.getLogger(UpgradeController.class);
	private static final String EXECUTION_FILE_NAME = "reports/meta.json";

	@Value("${spring.datasource.url}")
	private String dataSourceUrl;

	@Value("${spring.datasource.username}")
	private String dataUserName;

	@Value("${spring.datasource.password}")
	private String dataPassword;

	@PostConstruct
	public void upgrade() {
		final File metaFile = new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER),
				EXECUTION_FILE_NAME);
		if (!metaFile.exists()) {
			log.info("No need to upgrade persistency");
			return;
		}
		upgradeToFrom2To3(metaFile);

	}

	private void upgradeToFrom2To3(final File metaFile) {
		try {
			log.info("Starting to upgrade to version 3");
			long start = System.currentTimeMillis();
			final JdbcTemplate template = buildTemplate();
			final MetaFileReader reader = new MetaFileReader(metaFile.getAbsolutePath());
			final Map<Integer, OldMetadata> data = reader.read();
			final Worker worker = new Worker(template, data, true);
			worker.work();
			metaFile.renameTo(new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER),
					EXECUTION_FILE_NAME + ".upgrade.backup"));
			log.info("Finished upgrading to version 3 in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
		} catch (Exception e) {
			log.error("Failure in upgrading to version 3", e);
		}

	}

	private JdbcTemplate buildTemplate() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(dataSourceUrl);
		ds.setUser("sa");
		ds.setPassword("");
		JdbcTemplate template = new JdbcTemplate(ds);
		return template;
	}

}
