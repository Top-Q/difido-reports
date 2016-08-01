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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public abstract class AbstractDifidoReporter implements Reporter {

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:");

	private static final SimpleDateFormat TIME_AND_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");

	private Execution execution;

	private ScenarioNode currentTestScenario;

	private MachineNode currentMachine;

	private ScenarioNode currentClassScenario;

	private TestDetails testDetails;

	private HashMap<Integer, Integer> testCounter;

	private TestNode currentTest;

	private int index;

	private String executionUid;

	private String testClassName;
	
	private long lastWrite;

	private int totalPlannedTests = 0;

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
	private void addMachineToExecution(String host) {
		currentMachine = null;
		if (host == null) {
			currentMachine = new MachineNode(getMachineName());
			
		} else {
			currentMachine = new MachineNode(host);
		}
		currentMachine.setPlannedTests(totalPlannedTests);
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
		} else {
			currentMachine = lastMachine;
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
		if (!result.getTestClass().getName().equals(testClassName)) {
			testClassName = result.getTestClass().getName();
			startClassScenario(result);
		}
		String testName = result.getName();
		List<String> testParameters = getTestParameters(result);
		if (!testParameters.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(testName);
			sb.append("(");
			String tempString = testParameters.toString().replaceFirst("\\[", "");
			sb.append(tempString.substring(0, tempString.length() - 1));
			sb.append(")");
			testName = sb.toString();
		}

		currentTest = new TestNode(index++, testName, executionUid + "-" + index);
		currentTest.setClassName(testClassName);
		currentTest.setTimestamp(TIME_FORMAT.format(new Date(result.getStartMillis())));
		currentClassScenario.addChild(currentTest);
		testDetails = new TestDetails(testName, currentTest.getUid());
		testDetails.setTimeStamp(TIME_AND_DATE_FORMAT.format(new Date(result.getStartMillis())));
		if (result.getMethod().getDescription() != null) {
			testDetails.setDescription(result.getMethod().getDescription());
		}
		addPropertyIfExist("Class", result.getTestClass().getName());
		addPropertyIfExist("Groups", Arrays.toString(result.getMethod().getGroups()));
		for (String paramValue : testParameters) {
			testDetails.addParameter(paramValue, "");
		}

		int numOfAppearances = getAndUpdateTestHistory(result.getTestClass().getName() + testName);
		if (numOfAppearances > 0) {
			currentTest.setName(currentTest.getName() + " (" + ++numOfAppearances + ")");
		}
		updateTestDirectory();
		writeExecution(execution);
		writeTestDetails(testDetails);

	}

	private List<String> getTestParameters(ITestResult result) {
		List<String> testParameters = new ArrayList<String>();
		if (result.getParameters() != null) {
			for (Object parameter : result.getParameters()) {
				if (parameter != null) {
					testParameters.add(parameter.toString());
				}
			}
		}
		return testParameters;
	}

	private void startClassScenario(ITestResult result) {
		ScenarioNode scenario = new ScenarioNode(testClassName);
		currentTestScenario.addChild(scenario);
		currentClassScenario = scenario;
	}

	protected abstract void updateTestDirectory();

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
		reportLastTestException(result);
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

	private void reportLastTestException(ITestResult result) {
		if (null == result) {
			return;
		}

		// Get the test's last exception
		final Throwable e = result.getThrowable();
		if (null == e) {
			return;
		}

		// Log the test's last unhandled exception
		String title = null;
		String message = null;
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			e.printStackTrace(pw);
			title = ("The test ended with the following exception:");
			message = sw.toString();
		} catch (IOException e1) {
			title = ("The test ended with unknown exception");
		}
		if (e instanceof AssertionError) {
			log(title, message, Status.failure, ElementType.regular);
		} else {
			log(title, message, Status.error, ElementType.regular);
		}
	}

	@Override
	public void onStart(ITestContext context) {
		ScenarioNode scenario = new ScenarioNode(context.getName());
		currentMachine.addChild(scenario);
		currentTestScenario = scenario;
		// TODO: We want to avoid a case in which there is the same test class
		// in different tests and a new scenario class is not created
		testClassName = null;

	}

	/**
	 * Event for start of suite
	 * 
	 * @param suite
	 */
	@Override
	public void onStart(ISuite suite) {
		execution = null;
		totalPlannedTests =  getAllPlannedTestsCount(suite);
		updateIndex();
		generateUid();

		addMachineToExecution(suite.getHost());
		currentTest = null;
	}

	/**
	 * Event for end of suite
	 * 
	 * @param suite
	 */
	@Override
	public void onFinish(ISuite suite) {
	}

	@Override
	public void log(String title, String message, Status status, ElementType type) {
		if (null == testDetails) {
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
		currentTest.setStatus(status);
		if ((System.currentTimeMillis() - lastWrite) > 100){
			lastWrite = System.currentTimeMillis();
			writeTestDetails(testDetails);
		}
	}

	private ReportElement updateTimestampAndTitle(ReportElement element, String title) {
		element.setTime(TIME_FORMAT.format(new Date()));
		element.setTitle(title);
		return element;
	}
	
	
	private int getAllPlannedTestsCount(ISuite suite){
		int totalPlanned = 0;

		for (ITestNGMethod method : suite.getAllMethods()) {
			totalPlanned +=  getDataProviderCases(method);
		}
	//	System.out.println("Total tests planned with dataProviders:" + totalPlanned );
		return totalPlanned;
		
		
	}
	
	/**
	 * This method counts all the planned test cases using dataProviders for the given ITestNGMethod.
	 * @param method
	 * @return the number of cases, returns 1 if no dataProvider is found for the test.
	 */
	private int getDataProviderCases(ITestNGMethod method){
		int ret = 1;
		try {
			Method m = method.getConstructorOrMethod().getMethod();
			Object instance = method.getInstance();
			
			if (!m.isAnnotationPresent(Test.class)){
				return ret;
			}
			
			String dataProviderName =  m.getAnnotation(Test.class).dataProvider();
			if (null == dataProviderName || dataProviderName.isEmpty())
				return ret;
			
			Class<?> testClass =  instance.getClass();
			for (Method classM : testClass.getMethods()){
				 if (classM.isAnnotationPresent(DataProvider.class)) {
					 String thisDataProviderName = m.getAnnotation(Test.class).dataProvider();
                     if (thisDataProviderName.equals(dataProviderName)){
                    	 try {
                    		 Object[][] theData = (Object[][]) classM.invoke(instance);
                             ret = theData.length;
                           //  System.out.printf("Found %s cases for %s\n", ret, m.getName());
                             return ret;
						} catch (Exception e) {
							System.out.println("Exception in couting dataProvider cases" + e.getMessage());
						}
                     }
				 }
			}	 
			
			return ret;
			
			
		} catch (Exception e2) {
			System.out.println("Exception 2 in couting dataProvider cases" + e2.getMessage());
			return ret;
		}
	}
	

	public void addTestProperty(String name, String value) {
		if (null == testDetails) {
			return;
		}
		testDetails.addProperty(name, value);
	}

	/**
	 * Add free property to the whole run
	 * 
	 * @param name
	 * @param value
	 */
	public void addRunProperty(String name, String value) {
		if (null == currentClassScenario) {
			return;
		}
		log("Adding run proprty '" + name + "'='" + value + "'", null, Status.success, ElementType.regular);
		currentTestScenario.addScenarioProperty(name, value);
	}

	@Override
	public void onFinish(ITestContext context) {
		writeExecution(execution);
	}

	protected TestNode getCurrentTest() {
		return currentTest;
	}

	protected TestDetails getTestDetails() {
		return testDetails;
	}

	protected Execution getExecution() {
		return execution;
	}

}
