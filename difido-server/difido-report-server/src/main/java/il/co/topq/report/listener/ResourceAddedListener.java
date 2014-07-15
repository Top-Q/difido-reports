package il.co.topq.report.listener;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

public interface ResourceAddedListener {

	void executionAdded(Execution execution);

	void machineAdded(MachineNode machine);

	void scenarioAdded(ScenarioNode scenario);

	void testAdded(TestNode test);
	
	void testDetailsAdded(TestDetails details);

	void reportElementAdded(ReportElement element);

}
