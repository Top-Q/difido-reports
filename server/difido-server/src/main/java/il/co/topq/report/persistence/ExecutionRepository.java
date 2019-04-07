package il.co.topq.report.persistence;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import il.co.topq.difido.model.execution.Execution;

@Component
public class ExecutionRepository {

	private Map<Integer, Execution> executions;

	public ExecutionRepository() {
		executions = new HashMap<>();
	}

	public Execution findById(int id) {
		return executions.get(id);
	}

	public void save(int id, Execution execution) {
		executions.put(id, execution);
	}
	
	public void delete(int id) {
		executions.remove(id);
	}

}
