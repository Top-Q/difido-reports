package il.co.topq.report.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.listener.elastic.ESController;
import il.co.topq.report.listener.execution.ExecutionManager;
import il.co.topq.report.listener.html.HtmlViewGenerator;
import il.co.topq.report.listener.mail.MailController;
import il.co.topq.report.scheduled.ExecutionEnder;

public enum ListenersManager {

	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(ListenersManager.class);

	private List<ReportServerListener> listenersList = new CopyOnWriteArrayList<ReportServerListener>();

	private ListenersManager() {
		// TODO: This should be done by another way. Maybe injection

		if (Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_HTML_REPORTS)) {
			addListener(new HtmlViewGenerator());
		}
		if (Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_ELASTIC_SEARCH)) {
			addListener(new ESController());
		}
		if (Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_MAIL)) {
			addListener(new MailController());
		}
		addListener(ExecutionManager.INSTANCE);
	}

	public void addListener(ReportServerListener listener) {
		if (listener != null) {
			log.debug("Adding listener " + listener.getClass().getSimpleName());
			listenersList.add(listener);
		}
	}

	public void removeListener(ReportServerListener listener) {
		if (!listenersList.contains(listener)) {
			return;
		}
		log.debug("Removing listener " + listener.getClass().getSimpleName());
		listenersList.remove(listener);
	}

	public void notifyExecutionAdded(int executionId, Execution execution) {
		log.debug("Notifying that execution with id " + executionId + " was added");
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					try {
						((ResourceChangedListener) listener).executionAdded(executionId, execution);
					} catch (Throwable t) {
						log.error("Execption while notifying listner " + listener.getClass().getSimpleName(), t);
					}

				}
			}
		}

	}

	public void notifyExecutionDeleted(int executionId) {
		log.debug("Notifying that execution " + executionId + " was deleted");
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					try {
						((ResourceChangedListener) listener).executionDeleted(executionId);
					} catch (Throwable t) {
						log.error("Execption while notifying listner " + listener.getClass().getSimpleName(), t);
					}

				}
			}
		}

	}

	public void notifyMachineAdded(int executionId, MachineNode machine) {
		log.debug("Notifying that machine was added to execution with id " + executionId);
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				try {
					((ResourceChangedListener) listener).machineAdded(executionId, machine);

				} catch (Throwable t) {
					log.error("Execption while notifying listner " + listener.getClass().getSimpleName(), t);
				}

			}
		}
	}

	public void notifyTestDetailsAdded(int executionId, TestDetails details) {
		log.debug("Notifying that test details was added to execution with id " + executionId);
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				try {
					((ResourceChangedListener) listener).testDetailsAdded(executionId, details);

				} catch (Throwable t) {
					log.error("Execption while notifying listner " + listener.getClass().getSimpleName(), t);
				}

			}
		}
	}

	public void notifyFileAddedToTest(int executionId, String testUid, byte[] fileContent, String fileName) {
		log.debug("Notifying that file was added to test with UID " + testUid + " and execution " + executionId);
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					try {
						((ResourceChangedListener) listener).fileAddedToTest(executionId, testUid, fileContent,
								fileName);
					} catch (Throwable t) {
						log.error("Execption while notifying listner " + listener.getClass().getSimpleName(), t);
					}
				}
			}
		}

	}

	public void notifyExecutionEnded(int executionId, Execution execution) {
		log.debug("Notifying that execution with id " + executionId + " has ended");
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					try {
						((ResourceChangedListener) listener).executionEnded(executionId, execution);
					} catch (Throwable t) {
						log.error("Execption while notifying listner " + listener.getClass().getSimpleName(), t);
					}
				}
			}
		}
	}
}
