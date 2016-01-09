package il.co.topq.report.model.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.model.remote.ExecutionDetails;

public class ExecutionContainerImpl implements ExecutionContainer{
	
	private final Logger log = LoggerFactory.getLogger(ExecutionContainerImpl.class);
	
	public ExecutionContainerImpl(){
		
	}

	@Override
	public int addExecution(ExecutionDetails executionDetails) {
		log.info("Adding execution!!!!!!");
		return 0;
	}

	@Override
	public int addExecution() {
		return 0;
	}
	
	
	
}
