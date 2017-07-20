package il.co.topq.difido.binder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

public class HardCodedBinder implements Binder {

	private Execution execution;

	private List<TestDetails> testDetailsList = new ArrayList<TestDetails>();
	
	@Override
	public void process(File source) {
		execution = new Execution();
		MachineNode machine = new MachineNode();
		machine.setName("my machine");
		machine.setStatus(Status.success);
		execution.addMachine(machine);

		ScenarioNode scenario = new ScenarioNode("my scenario");
		machine.addChild(scenario);

		TestNode test = new TestNode("myTest", "1");
		scenario.addChild(test);

		TestDetails testDetails = new TestDetails("1");
		ReportElement element = new ReportElement(testDetails);
		element.setStatus(Status.success);
		element.setMessage("some message");
		element.setTitle("Some title");
		testDetails.addReportElement(element);
		testDetailsList.add(testDetails);
	}

	@Override
	public Execution getExecution() {
		return execution;
	}

	@Override
	public List<TestDetails> getTestDetails() {
		return testDetailsList;
	}

}
