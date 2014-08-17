package il.co.topq.report.controller;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.controller.listener.ResourceChangedListener;
import il.co.topq.report.model.Session;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * This class is responsible for closing the execution in case no events are
 * reached after a configured timeout
 * 
 * @author Itai Agmon
 *
 */
public class ExecutionEnderScheduler implements ResourceChangedListener {

	private static final Logger log = Logger.getLogger(ExecutionEnderScheduler.class.getName());
	private long lastUpdateTime;
	private boolean enabled = true;
	private boolean executionIsOpened;
	private Timer timer;
	private long maximumExecutionIdleTimeInMilli;

	public ExecutionEnderScheduler() {
		final String propValue = Configuration.INSTANCE.read(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		if (propValue == null || propValue.isEmpty()) {
			enabled = false;
			return;
		}
		try {
			maximumExecutionIdleTimeInMilli = Long.parseLong(propValue) * 1000;
		} catch (Throwable t) {
			log.warning("Failed parsing maximum execution idle time");
			enabled = false;
		}

	}

	@Override
	public void executionAdded(Execution execution) {
		if (!enabled) {
			return;
		}
		executionIsOpened = true;
		lastUpdateTime = System.currentTimeMillis();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				log.fine("Execution ender scheduler is checking if it should end the execution");
				if (!executionIsOpened) {
					return;
				}

				if (System.currentTimeMillis() - lastUpdateTime > (maximumExecutionIdleTimeInMilli / 1000)) {
					log.fine("Execution idle time has exceeded maximum of " + maximumExecutionIdleTimeInMilli / 1000
							+ " seconds. Ending execution");
					// Execution should be closed;
					final Execution execution = Session.INSTANCE.getLastActiveExecution();
					ListenersManager.INSTANCE.notifyExecutionEnded(execution);
				}

			}

		}, maximumExecutionIdleTimeInMilli, maximumExecutionIdleTimeInMilli / 4);

	}

	@Override
	public void executionEnded(Execution execution) {
		executionIsOpened = false;
		timer.cancel();

	}

	@Override
	public void machineAdded(MachineNode machine) {
		lastUpdateTime = System.currentTimeMillis();

	}

	@Override
	public void testDetailsAdded(TestDetails details) {
		lastUpdateTime = System.currentTimeMillis();
	}


}
