package il.co.topq.report.controller.listener;

import java.io.InputStream;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;

public interface ResourceChangedListener extends ReportServerListener {

	void executionAdded(int executionId, Execution execution);

	void executionDeleted(int executionId);

	void executionEnded(int executionId, Execution execution);

	void machineAdded(int executionId, MachineNode machine);

	void testDetailsAdded(int executionId, TestDetails details);
	
	void fileAddedToTest(int executionId, String testUid, InputStream fileInputStream, String fileName);

}
