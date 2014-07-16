package il.co.topq.report.controller.listener;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

public interface ResourceChangedListener extends ReportServerListener {

	void executionAdded(Execution execution);
	
	void executionEnded(Execution execution);

	void machineAdded(MachineNode machine);

	void scenarioAdded(ScenarioNode scenario);

	void testAdded(TestNode test);
	
	void testEnded(TestNode test);

	void testDetailsAdded(TestNode test, TestDetails details);

	void reportElementAdded(TestNode test, ReportElement element);

}
