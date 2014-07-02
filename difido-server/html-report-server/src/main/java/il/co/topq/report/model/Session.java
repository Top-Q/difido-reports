package il.co.topq.report.model;

import il.co.topq.report.model.execution.ReportedExecution;

import java.util.ArrayList;
import java.util.List;

public enum Session {
	INSTANCE;
	
	private List<ReportedExecution> executions;
	
	public int addExecution(){
		if (null == executions){
			executions = new ArrayList<ReportedExecution>();
		}
		executions.add(new ReportedExecution());
		return executions.size() - 1;
	}
	
	public ReportedExecution getExecution(){
		return getExecution(0);
	}
	
	public ReportedExecution getExecution(int index){
		if (null == executions){
			return null;
		}
		if (index >= executions.size()){
			//TODO: return error
		}
		return executions.get(index);
	}
	
	
}
