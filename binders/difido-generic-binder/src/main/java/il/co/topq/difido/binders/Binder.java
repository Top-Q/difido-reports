package il.co.topq.difido.binders;

import java.io.File;
import java.util.List;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;

public interface Binder {

	void process(File source) throws Exception;

	Execution getExecution();

	List<TestDetails> getTestDetails();

}
