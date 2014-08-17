package il.co.topq.report.controller.listener;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;

public interface ResourceChangedListener extends ReportServerListener {

	void executionAdded(Execution execution);
	
	void executionEnded(Execution execution);

	void machineAdded(MachineNode machine);

	void testDetailsAdded(TestDetails details);

}
