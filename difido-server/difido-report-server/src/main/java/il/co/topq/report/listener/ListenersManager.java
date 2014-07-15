package il.co.topq.report.listener;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ListenersManager {

	INSTANCE;

	private List<ResourceAddedListener> listenersList = new CopyOnWriteArrayList<ResourceAddedListener>();

	public void addListener(ResourceAddedListener listener) {
		listenersList.add(listener);
	}

	public void removeListener(ResourceAddedListener listener) {
		if (!listenersList.contains(listener)) {
			return;
		}
		listenersList.remove(listener);
	}

	public void notifyExecutionAdded(Execution execution) {
		synchronized (listenersList) {
			for (ResourceAddedListener listener : listenersList) {
				listener.executionAdded(execution);
			}
		}

	}

	public void notifyMachineAdded(MachineNode machine) {
		synchronized (listenersList) {
			for (ResourceAddedListener listener : listenersList) {
				listener.machineAdded(machine);
			}
		}
	}

	public void notifyScenarioAdded(ScenarioNode scenario) {
		synchronized (listenersList) {
			for (ResourceAddedListener listener : listenersList) {
				listener.scenarioAdded(scenario);
			}
		}

	}

	public void notifyTestAdded(TestNode test) {
		synchronized (listenersList) {
			for (ResourceAddedListener listener : listenersList) {
				listener.testAdded(test);
			}
		}

	}
	
	public void notifyTestDetailsAdded(TestDetails details){
		synchronized (listenersList) {
			for (ResourceAddedListener listener : listenersList) {
				listener.testDetailsAdded(details);
			}
			
		}
	}

	public void notifyReportElementAdded(ReportElement element) {
		synchronized (listenersList) {
			for (ResourceAddedListener listener : listenersList) {
				listener.reportElementAdded(element);
			}
		}

	}

}
