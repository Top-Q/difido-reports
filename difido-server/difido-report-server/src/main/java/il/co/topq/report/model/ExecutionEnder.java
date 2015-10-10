package il.co.topq.report.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.model.ExecutionManager.ExecutionMetaData;

public class ExecutionEnder implements ResourceChangedListener {

	private final Logger log = LoggerFactory.getLogger(ExecutionEnder.class);

	protected static final long THREAD_SLEEP_TIME = 30000;

	private Thread executionChecker;

	@Override
	public void executionAdded(int executionId, Execution execution) {
		final int maxExecutionIdleTimeout = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		if (maxExecutionIdleTimeout <= 0) {
			return;
		}
		log.debug("Starting execution ender thread with max execution idle timeout of " + maxExecutionIdleTimeout);
		if (executionChecker == null) {
			executionChecker = new Thread() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(THREAD_SLEEP_TIME);
						} catch (InterruptedException e) {
							// Not going to happen
						}
						log.debug("Waking up in order to search for executions that need to end");
						final ExecutionMetaData[] metaDataArr = ExecutionManager.INSTANCE.getAllMetaData();
						for (ExecutionMetaData meta : metaDataArr) {
							if (!meta.isActive()) {
								continue;
							}
							final int idleTime = (int) (System.currentTimeMillis() - meta.getLastAccessedTime()) / 1000;
							if (null == meta.getExecution()) {
								log.warn("Active meta data of execution with id " + meta.getId()
										+ " has no execution included");
							}
							if (idleTime > maxExecutionIdleTimeout) {
								log.debug("Execution with id " + meta.getId() + " idle time is " + idleTime
										+ " which exceeded the max idle time of " + maxExecutionIdleTimeout
										+ ". Disabling execution");
								ListenersManager.INSTANCE.notifyExecutionEnded(meta.getId(), meta.getExecution());
							}
						}
					}
				}
			};
			executionChecker.start();
		}

	}

	@Override
	public void executionEnded(int executionId, Execution execution) {
		// TODO Auto-generated method stub

	}

	@Override
	public void machineAdded(int executionId, MachineNode machine) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testDetailsAdded(int executionId, TestDetails details) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executionDeleted(int executionId) {
		
	}

}
