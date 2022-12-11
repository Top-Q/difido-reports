package il.co.topq.report.business.report;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;

@Service
public class ArchiveService {
	private static final Logger log = LoggerFactory.getLogger(ArchiveService.class);

	/**
	 * Archive to ZIP the HTML reports folder of the specified execution.
	 * 
	 * @param metadata
	 * 		Metadata of the execution to archive
	 *
	 * @return Path to temporary file contains the archived HTML reports or null
	 *         if problem occurred
	 */
	public java.nio.file.Path archiveReports(final ExecutionMetadata metadata) {
		final File source = getExecutionFolder(metadata);
		if (!source.exists()) {
			log.error("Report folder of execution " + metadata.getId() + " was not found");
			return null;
		}
		File destination = null;
		try {
			destination = File.createTempFile("execution" + metadata.getId() + "_", ".zip");
		} catch (IOException e) {
			log.error("Failed to create temp file", e);
			return null;
		}
		ZipUtil.pack(source, destination);
		log.debug("Temporary file with report of execution " + metadata.getId() + " was created in "
				+ destination.getAbsolutePath());
		final java.nio.file.Path destinationPath = destination.toPath();
		return destinationPath;
	}

	public File getExecutionFolder(ExecutionMetadata metadata) {
		return new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER) + File.separator
				+ Common.REPORTS_FOLDER_NAME + File.separator + metadata.getFolderName());
	}
}
