package il.co.topq.report.business.execution;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.AsyncActionQueue;

@Component
class MetadataFileSystemPersistency extends AbstactMetadataPersistency {

	private static final Logger log = LoggerFactory.getLogger(MetadataFileSystemPersistency.class);

	private static final String EXECUTION_FILE_NAME = "reports/meta.json";

	/**
	 * The time interval in milliseconds between the backups of the meta file.
	 */
	private static final long BACKUP_INTERVAL = 1000 * 60 * 60;
	/**
	 * The last time in milliseconds that the meta file was backed up
	 */
	private long lastBackedUp;

	private AsyncActionQueue queue;

	@Autowired
	public MetadataFileSystemPersistency(AsyncActionQueue queue) {
		super();
		this.queue = queue;
	}

	private File getExecutionMetaFile() {
		return new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER), EXECUTION_FILE_NAME);
	}

	@Override
	protected void writeToPersistency() {
		if (!isCacheInitialized()) {
			return;
		}
		final File metaFile = getExecutionMetaFile();
		if (!metaFile.exists()) {
			if (!metaFile.getParentFile().exists()) {
				if (!metaFile.getParentFile().mkdirs()) {
					log.error("Failed creating folder for execution meta file ");
					return;
				}
				try {
					if (!metaFile.createNewFile()) {
						log.error("Failed creating execution meta file");
						return;
					}
				} catch (IOException e) {
					log.error("Failed creating execution meta file", e);
					return;
				}
			}
		}
		queue.addAction(() -> {
			try {
				// We will create a temporary file and only after successful
				// write we
				// will move it to be the actual file.
				final File tempMetaFile = new File(metaFile.getParent(), metaFile.getName() + ".tmp");
				if (tempMetaFile.exists()) {
					if (!tempMetaFile.delete()) {
						log.warn("Failed to delete temp execution file " + tempMetaFile.getAbsolutePath());
					}
				}
				new ObjectMapper().writeValue(tempMetaFile, getExecutionsCache());
				if (tempMetaFile.length() == 0) {
					log.warn("Execution meta file '" + tempMetaFile.getAbsolutePath()
							+ "' length is 0 after serialiaztion. Aborting write");
					return;
				}
				Files.move(tempMetaFile.toPath(), metaFile.toPath(), REPLACE_EXISTING);
				backupIfNeeded(metaFile);
			} catch (IOException e) {
				log.error("Failed writing execution meta data", e);
			}
		});
	}

	private void backupIfNeeded(final File metaFile) {
		if ((System.currentTimeMillis() - lastBackedUp) > BACKUP_INTERVAL) {
			final File backupMetaFile = new File(metaFile.getParent(), metaFile.getName() + ".bu");
			try {
				Files.copy(metaFile.toPath(), backupMetaFile.toPath(), REPLACE_EXISTING);
			} catch (IOException e) {
				log.warn("Failed to create backup file to " + backupMetaFile.getAbsolutePath() + " due to "
						+ e.getMessage(), e);
			}
		}
		lastBackedUp = System.currentTimeMillis();
	}

	@Override
	protected void readFromPersistency() {
		if (isCacheInitialized()) {
			// We read it already
			return;
		}
		final File metaFile = getExecutionMetaFile();
		final File tempMetaFile = new File(metaFile.getParent(), metaFile.getName() + ".tmp");

		if (tempMetaFile.exists()) {
			// This means that the server was shutdown before it had the chance
			// to move the temp file to the final file.
			log.warn("Found temporary file " + tempMetaFile.getAbsolutePath());
			if ((!metaFile.exists() || metaFile.length() == 0) && tempMetaFile.length() != 0) {
				// Very rare. but we can recover from temp file.
				try {
					Files.move(tempMetaFile.toPath(), metaFile.toPath(), REPLACE_EXISTING);
				} catch (IOException e) {
					log.error(
							"Found temp meta file with content and empty meta file but failed to copy the temp to final",
							e);
				}
			}
		}
		if (metaFile.length() == 0) {
			log.warn("Found meta file but it is empty. Deleting the file");
			metaFile.delete();
		}

		if (!metaFile.exists()) {
			initCache();
			return;
		}

		try {
			Map<Integer, ExecutionMetadata> data = new ObjectMapper().readValue(metaFile,
					new TypeReference<Map<Integer, ExecutionMetadata>>() {
					});
			populateCache(data);
		} catch (IOException e) {
			log.error("Failed reading execution meta data file '" + metaFile.getAbsolutePath()
					+ "'. It seems to be corrupted. Check if meta.json.tmp file exists", e);
			initCache();
			return;
		}

	}

}