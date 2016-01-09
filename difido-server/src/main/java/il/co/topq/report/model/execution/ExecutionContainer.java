package il.co.topq.report.model.execution;

import il.co.topq.difido.model.remote.ExecutionDetails;

public interface ExecutionContainer {
	
	
	int addExecution(ExecutionDetails executionDetails);
	
	int addExecution();
	
	
}
