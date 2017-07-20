package il.co.topq.difido.engine;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.binders.Binder;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;

public class LocalReportEngine implements ReportEngine {

	private Binder binder;

	private final File source;
	
	private final File destination;
	
	private File currentLogFolder;
	
	public LocalReportEngine(File source, File destination) {
		this.source = source;
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
				e.printStackTrace();
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
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init(Binder binder) {
		this.binder = binder;
		copyFiles();
	}

	@Override
	public void run() throws Exception {
		binder.process(source);
		Execution execution = binder.getExecution();
		PersistenceUtils.writeExecution(execution, currentLogFolder);
		for (TestDetails details : binder.getTestDetails()) {
			if (null == details) {
				System.out.println("Null empty details");
			}
			PersistenceUtils.writeTest(details, currentLogFolder,
					new File(currentLogFolder, "tests" + File.separator + "test_" + details.getUid()));
		}
	}

}
