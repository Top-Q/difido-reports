package il.co.topq.report.controller.listener;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.model.Session;
import il.co.topq.report.view.HtmlViewGenerator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.w3c.dom.ls.LSInput;

public enum ListenersManager {

	INSTANCE;

	private List<ReportServerListener> listenersList = new CopyOnWriteArrayList<ReportServerListener>();

	private ListenersManager() {
		// TODO: This should be done by another way. Maybe injection
		listenersList.add(new HtmlViewGenerator());
		listenersList.add(Session.INSTANCE);
	}

	public void addListener(ReportServerListener listener) {
		if (listener != null) {
			listenersList.add(listener);
		}
	}

	public void removeListener(ReportServerListener listener) {
		if (!listenersList.contains(listener)) {
			return;
		}
		listenersList.remove(listener);
	}

	public void notifyExecutionAdded(Execution execution) {
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					((ResourceChangedListener) listener).executionAdded(execution);

				}
			}
		}

	}

	public void notifyMachineAdded(MachineNode machine) {
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				((ResourceChangedListener) listener).machineAdded(machine);

			}
		}
	}

	public void notifyScenarioAdded(ScenarioNode scenario) {
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				((ResourceChangedListener) listener).scenarioAdded(scenario);

			}
		}

	}

	public void notifyTestAdded(TestNode test) {
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				((ResourceChangedListener) listener).testAdded(test);

			}
		}

	}

	public void notifyTestEnded(TestNode test) {
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				((ResourceChangedListener) listener).testEnded(test);

			}
		}

	}

	public void notifyTestDetailsAdded(TestNode test, TestDetails details) {
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				((ResourceChangedListener) listener).testDetailsAdded(test, details);

			}
		}
	}

	public void notifyReportElementAdded(TestNode test, ReportElement element) {
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					((ResourceChangedListener) listener).reportElementAdded(test, element);

				}
			}
		}
	}

	public void notifyExecutionEnded(Execution execution) {
		synchronized (listenersList) {
			for (ReportServerListener listener : listenersList) {
				if (listener instanceof ResourceChangedListener) {
					((ResourceChangedListener) listener).executionEnded(execution);

				}
			}
		}
	}

}
