package il.co.topq.difido.engine;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.binder.Binder;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;

public class RemoteReportEngine implements ReportEngine {

	private Logger log = LoggerFactory.getLogger(RemoteReportEngine.class);

	private final String host;

	private final int port;

	private Binder binder;

	private File source;

	private final String executionDescription;

	private DifidoClient client;

	public RemoteReportEngine(String host, int port, String executionDescription) {
		this.host = host;
		this.port = port;
		this.executionDescription = executionDescription;
	}

	@Override
	public void init(File source, Binder binder) {
		this.binder = binder;
		this.source = source;
		client = new DifidoClient(host, port);
	}

	@Override
	public void run() {
		try {
			binder.process(source);
		} catch (Exception e) {
			log.error("Binder failed to process " + binder.getClass().getName());
			return;
		}
		final Execution execution = binder.getExecution();

		int executionId = -1;
		try {
			executionId = client.addExecution(new ExecutionDetails(executionDescription, false));
		} catch (Exception e) {
			log.error("Failed to add execution due to " + e.getMessage());
			return;
		}
		try {
			client.addMachine(executionId, execution.getLastMachine());
		} catch (Exception e) {
			log.error("Failed to add machine due to " + e.getMessage());
		}

		if (binder.getTestDetails() == null) {
			log.warn("Test details list is null");
			return;

		}
		for (TestDetails details : binder.getTestDetails()) {
			if (null == details) {
				log.warn("Recieved null test details");
				continue;
			}
			try {
				client.addTestDetails(executionId, details);
			} catch (Exception e) {
				log.error("Failed to add test details due to " + e.getMessage());
			}
		}
		try {
			client.endExecution(executionId);
		} catch (Exception e) {
			log.error("Failed to end execution with id " + executionId + " due to " + e.getMessage());
		}
	}

}
