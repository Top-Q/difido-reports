package il.co.topq.report.business.execution;

import java.util.List;

public interface MetadataPersistency {
	
	void add(ExecutionMetadata metadata);
	
	ExecutionMetadata get(int executionId);
	
	List<ExecutionMetadata> getAll();

	void remove(int id);

	void update(ExecutionMetadata metadata);

	int getMaxId();
	
	void dump();

}
