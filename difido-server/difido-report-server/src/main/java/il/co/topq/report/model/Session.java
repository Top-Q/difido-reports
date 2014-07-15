package il.co.topq.report.model;

import il.co.topq.difido.model.execution.Execution;

import java.util.ArrayList;
import java.util.List;

public enum Session {
	INSTANCE;

	private List<Execution> executions;

	public int addExecution() {
		if (null == executions) {
			executions = new ArrayList<Execution>();
		}
		Execution execution = new Execution();
		executions.add(execution);
		return executions.indexOf(execution);
	}

	public Execution getExecution() {
		return getExecution(0);
	}

	public Execution getExecution(int index) {
		if (null == executions) {
			return null;
		}
		if (index >= executions.size()) {
			// TODO: return error
		}
		return executions.get(index);
	}
	
	public void flush(){
		executions = null;
	}

}
