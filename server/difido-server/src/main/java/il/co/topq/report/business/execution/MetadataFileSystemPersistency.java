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
import il.co.topq.report.business.AsyncActionQueue.AsyncAction;

@Component
class MetadataFileSystemPersistency extends AbstactMetadataPersistency {

	private final Logger log = LoggerFactory.getLogger(MetadataFileSystemPersistency.class);

	private static final String EXECUTION_FILE_NAME = "reports/meta.json";

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
		final File executionMetaFile = getExecutionMetaFile();
		if (!executionMetaFile.exists()) {
			if (!executionMetaFile.getParentFile().exists()) {
				if (!executionMetaFile.getParentFile().mkdirs()) {
					log.error("Failed creating folder for execution meta file ");
					return;
				}
				try {
					if (!executionMetaFile.createNewFile()) {
						log.error("Failed creating execution meta file");
						return;
					}
				} catch (IOException e) {
					log.error("Failed creating execution meta file", e);
					return;
				}
			}
		}
		queue.addAction(new AsyncAction() {
			@Override
			public void execute() {
				try {
					// We will create a temporary file and only after successful
					// write we
					// will move it to be the actual file.
					final File executionTempMetaFile = new File(executionMetaFile.getParent(),
							executionMetaFile.getName() + ".tmp");
					new ObjectMapper().writeValue(executionTempMetaFile, getExecutionsCache());
					Files.move(executionTempMetaFile.toPath(), executionMetaFile.toPath(), REPLACE_EXISTING);
				} catch (IOException e) {
					log.error("Failed writing execution meta data", e);
				}
			}
		});
	}

	@Override
	protected void readFromPersistency() {
		if (isCacheInitialized()) {
			// We read it already
			return;
		}
		final File metaFile = getExecutionMetaFile();
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
			log.error("Failed reading execution meta data file.", e);
			initCache();
			return;
		}

	}

}