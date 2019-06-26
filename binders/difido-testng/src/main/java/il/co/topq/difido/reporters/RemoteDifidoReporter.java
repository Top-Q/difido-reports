package il.co.topq.difido.reporters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.testng.ISuite;

import il.co.topq.difido.ZipUtils;
import il.co.topq.difido.config.RemoteDifidoConfig;
import il.co.topq.difido.config.RemoteDifidoConfig.RemoteDifidoOptions;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;

public class RemoteDifidoReporter extends AbstractDifidoReporter {

	private static final Logger log = Logger.getLogger(RemoteDifidoReporter.class.getName());

	private static final int MAX_NUM_OF_ALLOWED_FAILURES = 10;

	private boolean enabled;

	private DifidoClient client;

	private int executionId;

	private int machineId;

	private int numOfFailures;

	private RemoteDifidoConfig difidoConfig;

	private Set<String> extentionsToSkip = null;

	/**
	 * When files are added in the setup phase, there is no test context and no
	 * test folder that they can be copied to. In those cases we keep the files
	 * in list so we could send it to the server later on
	 */
	private List<File> bufferedFiles = new ArrayList<File>();

	public RemoteDifidoReporter() {
		super();
		// We are doing it because we need that the file of the Difido
		// properties to be created if it is not exists.
		new RemoteDifidoConfig();

	}

	@Override
	public String getName() {
		return "RemoteDifidoReporter";
	}

	/**
	 * Event for end of suite
	 * 
	 * @param suite
	 */
	@Override
	public void onFinish(ISuite suite) {
		// TODO: Find a place to call it.

		// We are not using shared execution, that means that we are the only
		// one that are using it and we just ended with it, so let's set it to
		// not active
		if (executionId > 0 && !difidoConfig.getPropertyAsBoolean(RemoteDifidoOptions.USE_SHARED_EXECUTION)) {
			try {
				client.endExecution(executionId);
			} catch (Exception e) {
				System.out.println("Failed to close execution with id " + executionId);
			}
			executionId = -1;
		}

	}

	public void onStart(ISuite suite) {
		super.onStart(suite);
		difidoConfig = new RemoteDifidoConfig();
		String host = null;
		int port = 0;
		try {
			enabled = Boolean.parseBoolean(difidoConfig.getPropertyAsString(RemoteDifidoOptions.ENABLED));
			if (!enabled) {
				return;
			}
			host = difidoConfig.getPropertyAsString(RemoteDifidoOptions.HOST);
			port = Integer.parseInt(difidoConfig.getPropertyAsString(RemoteDifidoOptions.PORT));
			client = new DifidoClient(host, port);
			executionId = prepareExecution();
			machineId = client.addMachine(executionId, getExecution().getLastMachine());
			enabled = true;
			log.fine(RemoteDifidoReporter.class.getName() + " was initialized successfully");
		} catch (Throwable t) {
			enabled = false;
			log.warning("Failed to init " + RemoteDifidoReporter.class.getName() + "connection with host '" + host + ":"
					+ port + "' due to " + t.getMessage());
		}

	}

	private int prepareExecution() throws Exception {
		// Fetching properties
		final boolean appendToExistingExecution = difidoConfig
				.getPropertyAsBoolean(RemoteDifidoOptions.APPEND_TO_EXISTING_EXECUTION);
		final boolean useSharedExecution = difidoConfig.getPropertyAsBoolean(RemoteDifidoOptions.USE_SHARED_EXECUTION);
		final String description = difidoConfig.getPropertyAsString(RemoteDifidoOptions.DESCRIPTION);
		final int id = difidoConfig.getPropertyAsInt(RemoteDifidoOptions.EXISTING_EXECUTION_ID);
		final boolean forceNewExecution = difidoConfig.getPropertyAsBoolean(RemoteDifidoOptions.FORCE_NEW_EXECUTION);
		final HashMap<String, String> properties = difidoConfig.getPropertyAsMap(RemoteDifidoOptions.EXECUTION_PROPETIES);

		if (appendToExistingExecution && !forceNewExecution) {
			if (id >= 0) {
				return id;
			}
			if (executionId > 0) {
				return executionId;
			}

		}
		ExecutionDetails details = new ExecutionDetails(description, useSharedExecution);
		details.setForceNew(forceNewExecution);
		details.setExecutionProperties(properties);
		return client.addExecution(details);
	}

	@Override
	protected void onScenarioStart(ScenarioNode scenario) {
		// Unused
	}

	@Override
	protected void writeTestDetails(TestDetails testDetails) {
		if (!enabled) {
			return;
		}
		try {
			client.addTestDetails(executionId, testDetails);
		} catch (Exception e) {
			log.warning("Failed updating test details to remote server due to " + e.getMessage());
			checkIfNeedsToDisable();
		}

	}

	@Override
	protected void writeExecution(Execution execution) {
		if (!enabled) {
			return;
		}

		try {
			client.updateMachine(executionId, machineId, execution.getLastMachine());
		} catch (Exception e) {
			log.warning("Failed updating test details to remote server due to " + e.getMessage());
			checkIfNeedsToDisable();
		}
	}

	private void checkIfNeedsToDisable() {
		numOfFailures++;
		if (numOfFailures > MAX_NUM_OF_ALLOWED_FAILURES) {
			log.warning("Communication to server has failed more then " + MAX_NUM_OF_ALLOWED_FAILURES
					+ ". Disabling report reporter");
			enabled = false;
		}
	}

	@Override
	public void addFile(File file) {
		if (!enabled) {
			return;
		}
		if (file == null || !file.exists()) {
			return;
		}
		if (isInSetup()) {
			bufferedFiles.add(file);
		} else {
			sendFileToServer(file);
		}
	}

	private void sendFileToServer(File file) {
		try {

			int thresholdInBytes = difidoConfig.getPropertyAsInt(RemoteDifidoOptions.COMPRESS_FILES_ABOVE);
			if (thresholdInBytes > 0 && file.length() > thresholdInBytes && isCompressable(file)){
				
				//zip the file to an in-memory byte array and upload to server 
				//adding .gz to the original fileName;
				byte[] zipped = ZipUtils.gzipToBytesArray(file);
				if (null != zipped){
					client.addFile(executionId, getTestDetails().getUid(), zipped, file.getName().concat(".gz"));
				}
				else {
					log.warning("Failed to zip file on the fly, uploading original");
					client.addFile(executionId, getTestDetails().getUid(), file);
				}
			}
			else {
				client.addFile(executionId, getTestDetails().getUid(), file);
			}
		} catch (Exception e) {
			log.warning("Failed uploading file " + file.getName() + " to remote server due to " + e.getMessage());
		}
	}
	
	/**
	 * checks whether the file extension is 'blacklisted' for compression
	 */
	private boolean isCompressable(File f){
		String ext = FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase();
		return !getSkippedExtensions().contains(ext);
	}
	
	private Set<String> getSkippedExtensions(){
		initSkippedExtensions();
		return this.extentionsToSkip;
	}
	
	private void initSkippedExtensions(){
		if (this.extentionsToSkip != null)
			return;
		
		this.extentionsToSkip = new LinkedHashSet<>();
		List<String> extentionsList = difidoConfig.getPropertyAsList(RemoteDifidoOptions.DONT_COMPRESS_EXTENSIONS);
		for (String extension : extentionsList){
			int startFrom = extension.lastIndexOf(".") + 1; //strip off any irrelevant file parts a user may have entered (e.g. tar.gz, or *.exe)
			extentionsToSkip.add(extension.toLowerCase().substring(startFrom));
		}
	}
	

	/**
	 * Elements that are created in setup phases, before test context is created
	 * are stored and flushed in the beginning of the test. <br>
	 * We use this to send files that were also added in the setup phase to the
	 * server.
	 */
	@Override
	protected void flushBufferedElements(String elementsDescription) {
		super.flushBufferedElements(elementsDescription);
		if (!bufferedFiles.isEmpty()) {
			for (File file : bufferedFiles) {
				sendFileToServer(file);
			}
			bufferedFiles.clear();
		}
	}

	@Override
	protected void updateTestDirectory() {
		// Since we are not using the file system, there is no point in setting
		// the current test folder
	}

	@Override
	public File getCurrentTestFolder() {
		return null;
	}

	protected int getExecutionId() {
		return executionId;
	}

}
