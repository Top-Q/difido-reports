package il.co.topq.report.controller.listener;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.ExecutionEnderScheduler;
import il.co.topq.report.model.Session;
import il.co.topq.report.view.HtmlViewGenerator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ListenersManager {

	INSTANCE;

	private List<ReportServerListener> listenersList = new CopyOnWriteArrayList<ReportServerListener>();

	private ListenersManager() {
		// TODO: This should be done by another way. Maybe injection
		listenersList.add(HtmlViewGenerator.getInstance());
		listenersList.add(new ExecutionEnderScheduler());
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


	public void notifyTestDetailsAdded(TestDetails details) {
		for (ReportServerListener listener : listenersList) {
			if (listener instanceof ResourceChangedListener) {
				((ResourceChangedListener) listener).testDetailsAdded(details);

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
