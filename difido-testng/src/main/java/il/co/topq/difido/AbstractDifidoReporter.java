package il.co.topq.difido;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.NodeWithChildren;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.testng.ITestContext;
import org.testng.ITestResult;

public abstract class AbstractDifidoReporter implements Reporter {

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:");

	private static final SimpleDateFormat TIME_AND_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");

	private Execution execution;

	private ScenarioNode currentScenario;

	private TestDetails testDetails;

	private HashMap<Integer, Integer> testCounter;

	private TestNode currentTest;

	private int index;

	private String executionUid;

	protected void generateUid() {
		executionUid = String.valueOf(new Random().nextInt(1000)) + String.valueOf(System.currentTimeMillis() / 1000);
	}

	private void updateIndex() {
		if (null == execution) {
			index = 0;
			return;
		}
		if (execution.getMachines() == null || execution.getMachines().size() == 0) {
			index = 0;
			return;
		}
		for (MachineNode machine : execution.getMachines()) {
			for (Node child : machine.getChildren(true)) {
				if (!(child instanceof NodeWithChildren)) {
					index++;
				}
			}
		}
	}

	/**
	 * If no execution exists. Meaning, we are not appending to an older
	 * execution; A new execution would be created. If the execution is new,
	 * will add a new reported machine instance. If we are appending to an older
	 * execution, and the machine is the same as the machine the execution were
	 * executed on, will append the results to the last machine and will not
	 * create a new one.
	 * 
	 * @param context
	 * 
	 */
	private void addMachineToExecution(ITestContext context) {
		MachineNode currentMachine = null;
		if (context.getHost() == null) {
			currentMachine = new MachineNode(getMachineName());
		} else {
			currentMachine = new MachineNode(context.getHost());
		}
		if (null == execution) {
			execution = new Execution();
			execution.addMachine(currentMachine);
			return;
		}
		// We are going to append to existing execution
		MachineNode lastMachine = execution.getLastMachine();
		if (null == lastMachine || null == lastMachine.getName()) {
			// Something is wrong. We don't have machine in the existing
			// execution. We need to add a new one
			execution.addMachine(currentMachine);
			return;
		}
		if (!lastMachine.getName().equals(currentMachine.getName())) {
			// The execution happened on machine different from the current
			// machine, so we will create a new machine
			execution.addMachine(currentMachine);
		}

	}

	private static String getMachineName() {
		String machineName;
		try {
			machineName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			machineName = "localhost";
		}
		return machineName;
	}

	protected abstract void writeTestDetails(TestDetails testDetails);

	protected abstract void writeExecution(Execution execution);

	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getName();
		currentTest = new TestNode(index++, testName, executionUid + "-" + index);
		currentTest.setTimestamp(TIME_FORMAT.format(new Date(result.getStartMillis())));
		currentScenario.addChild(currentTest);
		testDetails = new TestDetails(testName, currentTest.getUid());
		testDetails.setTimeStamp(TIME_AND_DATE_FORMAT.format(new Date(result.getStartMillis())));
		if (result.getMethod().getDescription() != null) {
			testDetails.setDescription(result.getMethod().getDescription());
		}
		addPropertyIfExist("Class", result.getTestClass().getName());
		addPropertyIfExist("Groups", Arrays.toString(result.getMethod().getGroups()));
		if (result.getParameters() != null) {
			for (Object parameter : result.getParameters()) {
				if (parameter != null) {
					testDetails.addParameter(parameter.toString(), "");
				}
			}
		}
		int numOfAppearances = getAndUpdateTestHistory(result.getName());
		if (numOfAppearances > 0) {
			currentTest.setName(currentTest.getName() + " (" + ++numOfAppearances + ")");
		}
		updateTestDirectory();
		writeExecution(execution);
		writeTestDetails(testDetails);

	}

	protected abstract void updateTestDirectory();

	protected abstract void filesWereAddedToReport(File[] files);

	private int getAndUpdateTestHistory(final Object bb) {
		if (testCounter == null) {
			testCounter = new HashMap<>();
		}
		final int key = bb.hashCode();
		if (testCounter.containsKey(key)) {
			testCounter.put(key, testCounter.get(key) + 1);
		} else {
			testCounter.put(key, 0);
		}
		return testCounter.get(key);
	}

	private void addPropertyIfExist(String propertyName, String property) {
		if (property != null) {
			testDetails.addProperty(propertyName, property);
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		currentTest.setStatus(Status.success);
		onTestEnd(result);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		currentTest.setStatus(Status.failure);
		onTestEnd(result);

	}

	@Override
	public void onTestSkipped(ITestResult result) {
		currentTest.setStatus(Status.warning);
		onTestEnd(result);

	}

	private void onTestEnd(ITestResult result) {
		currentTest.setDuration(result.getEndMillis() - result.getStartMillis());
		writeTestDetails(testDetails);
	}

	@Override
	public void onStart(ITestContext context) {
		execution = null;
		updateIndex();
		generateUid();

		addMachineToExecution(context);
		currentScenario = new ScenarioNode(context.getSuite().getName());
		execution.getLastMachine().addChild(currentScenario);
		currentTest = null;
	}

	@Override
	public void log(String title, String message, Status status, ElementType type) {
		if (null == testDetails){
			return;
		}
		ReportElement element = new ReportElement();
		element = updateTimestampAndTitle(element, title);
		element.setMessage(message);
		element.setStatus(status);
		if (null == type) {
			type = ElementType.regular;
		}
		element.setType(type);
		testDetails.addReportElement(element);
		writeTestDetails(testDetails);
	}

	private ReportElement updateTimestampAndTitle(ReportElement element, String title) {
		element.setTime(TIME_FORMAT.format(new Date()));
		element.setTitle(title);
		return element;
	}

	@Override
	public void onFinish(ITestContext context) {
		writeExecution(execution);
	}

	protected TestNode getCurrentTest() {
		return currentTest;
	}

}
