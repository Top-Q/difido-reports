package il.co.topq.difido.engine;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.binder.Binder;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;

public class LocalReportEngine implements ReportEngine {

	private Logger log = LoggerFactory.getLogger(LocalReportEngine.class);
	
	private Binder binder;

	private File source;
	
	private final File destination;
	
	private File currentLogFolder;
	
	public LocalReportEngine(File destination) {
		this.destination = destination;
	}

	private void copyFiles() {
		if (!destination.exists()) {
			destination.mkdirs();
		}
		currentLogFolder = new File(destination, "current");
		if (currentLogFolder.exists()) {
			try {
				FileUtils.deleteDirectory(currentLogFolder);
			} catch (IOException e) {
				log.error("Failed to delete current folder due to " + e.getMessage());
			}
		}
		final File templateFolder = new File(destination, "template");
		if (!templateFolder.exists() || !(new File(templateFolder, "index.html").exists())) {
			PersistenceUtils.copyResources(templateFolder);
		}
		final File testDetailsHtmlFile = new File(currentLogFolder, PersistenceUtils.TEST_DETAILS_HTML_FILE);
		final File indexFile = new File(currentLogFolder, "index.html");
		if (!testDetailsHtmlFile.exists() && !indexFile.exists()) {
			try {
				FileUtils.copyDirectory(templateFolder, currentLogFolder);
			} catch (IOException e) {
				log.error("Failed to copy template folder due to " + e.getMessage());
			}
		}
	}

	@Override
	public void init(File source, Binder binder) {
		this.source = source;
		this.binder = binder;
		copyFiles();
	}

	@Override
	public void run() throws Exception {
		binder.process(source);
		Execution execution = binder.getExecution();
		PersistenceUtils.writeExecution(execution, currentLogFolder);
		if (binder.getTestDetails() == null) {
			log.warn("Test details list is null");
			return;
			
		}
		for (TestDetails details : binder.getTestDetails()) {
			if (null == details) {
				log.warn("Recieved null test details");
				continue;
			}
			PersistenceUtils.writeTest(details, currentLogFolder,
					new File(currentLogFolder, "tests" + File.separator + "test_" + details.getUid()));
		}
	}

}
