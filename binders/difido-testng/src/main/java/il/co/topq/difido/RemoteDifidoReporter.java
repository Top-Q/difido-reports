package il.co.topq.difido;

import il.co.topq.difido.RemoteDifidoProperties.RemoteDifidoOptions;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.testng.ISuite;

public class RemoteDifidoReporter extends AbstractDifidoReporter {

	private static final Logger log = Logger.getLogger(RemoteDifidoReporter.class.getName());

	private static final int MAX_NUM_OF_ALLOWED_FAILURES = 10;

	private boolean enabled;

	private DifidoClient client;

	private int executionId;

	private int machineId;

	private int numOfFailures;

	private RemoteDifidoProperties difidoProps;

	private ExecutionDetails details;

	public RemoteDifidoReporter() {
		super();
		// We are doing it because we need that the file of the Difido
		// properties to be created if it is not exists.
		new RemoteDifidoProperties();

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
		if (executionId > 0 && !difidoProps.getPropertyAsBoolean(RemoteDifidoOptions.USE_SHARED_EXECUTION)) {
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
		difidoProps = new RemoteDifidoProperties();
		String host = null;
		int port = 0;
		try {
			enabled = Boolean.parseBoolean(difidoProps.getPropertyAsString(RemoteDifidoOptions.ENABLED));
			if (!enabled) {
				return;
			}
			host = difidoProps.getPropertyAsString(RemoteDifidoOptions.HOST);
			port = Integer.parseInt(difidoProps.getPropertyAsString(RemoteDifidoOptions.PORT));
			client = new DifidoClient(host, port);
			executionId = prepareExecution();
			machineId = client.addMachine(executionId, getExecution().getLastMachine());
			enabled = true;
			log.fine(RemoteDifidoReporter.class.getName() + " was initialized successfully");
		} catch (Throwable t) {
			enabled = false;
			log.warning("Failed to init " + RemoteDifidoReporter.class.getName() + "connection with host '" + host
					+ ":" + port + "' due to " + t.getMessage());
		}

	}

	private int prepareExecution() throws Exception {
		// Fetching properties
		final boolean appendToExistingExecution = difidoProps
				.getPropertyAsBoolean(RemoteDifidoOptions.APPEND_TO_EXISTING_EXECUTION);
		final boolean useSharedExecution = difidoProps.getPropertyAsBoolean(RemoteDifidoOptions.USE_SHARED_EXECUTION);
		final String description = difidoProps.getPropertyAsString(RemoteDifidoOptions.DESCRIPTION);
		final int id = difidoProps.getPropertyAsInt(RemoteDifidoOptions.EXISTING_EXECUTION_ID);
		final boolean forceNewExecution = difidoProps.getPropertyAsBoolean(RemoteDifidoOptions.FORCE_NEW_EXECUTION);
		final Map<String, String> properties = difidoProps.getPropertyAsMap(RemoteDifidoOptions.EXECUTION_PROPETIES);

		if (appendToExistingExecution && !forceNewExecution) {
			if (id >= 0) {
				return id;
			}
			if (executionId > 0) {
				return executionId;
			}

		}
		details = new ExecutionDetails(description, useSharedExecution);
		details.setForceNew(forceNewExecution);
		details.setExecutionProperties(properties);
		return client.addExecution(details);
	}

	/**
	 * We want to add all the execution properties for each scenario. This will
	 * eventually appear in the ElasticSearch
	 * 
	 * @param scenario
	 */
	protected void addScenarioProperties(ScenarioNode scenario) {
		// If the execution is shared, and we were not responsible for creating
		// the execution, the execution details in this stage will be null.
		if (details != null && details.getExecutionProperties() != null) {
			for (String key : details.getExecutionProperties().keySet()) {
				scenario.addScenarioProperty(key, details.getExecutionProperties().get(key));
			}
		}
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
		try {
			client.addFile(executionId, getTestDetails().getUid(), file);
		} catch (Exception e) {
			log.warning("Failed uploading file " + file.getName() + " to remote server due to " + e.getMessage());
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

}
