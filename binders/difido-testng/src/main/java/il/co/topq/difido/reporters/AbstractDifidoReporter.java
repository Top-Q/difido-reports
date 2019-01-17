package il.co.topq.difido.reporters;

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
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import il.co.topq.difido.config.DifidoConfig;
import il.co.topq.difido.config.DifidoConfig.DifidoOptions;
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

public abstract class AbstractDifidoReporter implements Reporter {

	private static final Logger log = Logger.getLogger(AbstractDifidoReporter.class.getName());

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private static final SimpleDateFormat REPORT_ELEMENT_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

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

	private DifidoConfig config;

	private List<ReportElement> bufferedElements;

	private Map<String, String> bufferedTestProperties;

	private Map<String, String> bufferedRunProperties;

	private boolean inSetup;

	private boolean inTeardown;

	public AbstractDifidoReporter() {
		config = new DifidoConfig();
		bufferedElements = new ArrayList<ReportElement>();
		bufferedTestProperties = new HashMap<>();
		bufferedRunProperties = new HashMap<>();

	}

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
		final Date date = new Date(result.getStartMillis());
		currentTest.setTimestamp(TIME_FORMAT.format(date));
		currentTest.setDate(DATE_FORMAT.format(date));
		currentClassScenario.addChild(currentTest);
		testDetails = new TestDetails(currentTest.getUid());
		if (result.getMethod().getDescription() != null) {
			currentTest.setDescription(result.getMethod().getDescription());
		}
		addPropertyIfExist("Class", result.getTestClass().getName());
		addPropertyIfExist("Groups", Arrays.toString(result.getMethod().getGroups()));
		int paramCounter = 0;
		for (String paramValue : testParameters) {
			currentTest.addParameter("param" + paramCounter++, paramValue);
		}

		int numOfAppearances = getAndUpdateTestHistory(result.getTestClass().getName() + testName);
		if (numOfAppearances > 0) {
			currentTest.setName(currentTest.getName() + " (" + ++numOfAppearances + ")");
		}
		updateTestDirectory();
		writeExecution(execution);
		flushBufferedElements("Setup");
		writeTestDetails(testDetails);

	}

	/**
	 * Writing all the buffered elements that was stored in the configuration
	 * stages
	 * 
	 * @param elementsDescription
	 *            The description of the phase. e.g. 'setup'
	 */
	protected void flushBufferedElements(String elementsDescription) {
		log.fine("About to flush buffered elements");
		if (!bufferedElements.isEmpty()) {
			log.fine("Found " + bufferedElements.size() + " buffered elements");
			log(elementsDescription, null, Status.success, ElementType.startLevel);
			for (ReportElement element : bufferedElements) {
				log(element);
			}
			bufferedElements.clear();
			log(null, null, Status.success, ElementType.stopLevel);
		}
		if (!bufferedRunProperties.isEmpty()) {
			log.fine("Found " + bufferedRunProperties.size() + " buffered run properties");
			bufferedRunProperties.keySet().stream().forEach(key -> addRunProperty(key, bufferedRunProperties.get(key)));
			bufferedRunProperties.clear();
		}
		if (!bufferedTestProperties.isEmpty()) {
			log.fine("Found " + bufferedTestProperties.size() + " buffered test properties");
			bufferedTestProperties.keySet().stream()
					.forEach(key -> addTestProperty(key, bufferedTestProperties.get(key)));
			bufferedTestProperties.clear();
		}

		// We need to make sure that the report messages are written.
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
		onScenarioStart(currentClassScenario);
	}

	/**
	 * Event that is called when a new scenario is created
	 * 
	 * @param scenario
	 */
	protected abstract void onScenarioStart(ScenarioNode scenario);

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
			currentTest.addProperty(propertyName, property);
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
		onTestStart(result);
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
		totalPlannedTests = getAllPlannedTestsCount(suite);
		updateIndex();
		generateUid();

		addMachineToExecution(suite.getHost());
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
	public void beforeTeardown(IInvokedMethod method, ITestResult testResult) {
		inTeardown = true;
	}

	@Override
	public void beforeSetup(IInvokedMethod method, ITestResult testResult) {
		inSetup = true;
	}

	@Override
	public void afterTeardown(IInvokedMethod method, ITestResult testResult) {
		logIfFailureOccuredInConfiguration(testResult);
		inTeardown = false;
		flushBufferedElements("Teardown");
	}

	@Override
	public void afterSetup(IInvokedMethod method, ITestResult testResult) {
		logIfFailureOccuredInConfiguration(testResult);
		inSetup = false;
	}

	/**
	 * In case the setup or teardown step failed, we would like to log the
	 * exception as warning
	 * 
	 * @param testResult
	 */
	private void logIfFailureOccuredInConfiguration(ITestResult testResult) {
		if (!testResult.isSuccess()) {
			if (testResult.getThrowable() != null) {
				log(testResult.getThrowable().getMessage(), Arrays.toString(testResult.getThrowable().getStackTrace()),
						Status.warning, ElementType.regular);
			}
		}
	}

	private void log(ReportElement element) {
		if (inSetup || inTeardown) {
			// We are in setup phase. We will store the elements and add it to
			// the test details when the actual test will start
			if (Status.error == element.getStatus() || Status.failure == element.getStatus()) {
				// Failures and errors that happens in the setup or tear down
				// phases should be marked as warning
				element.setStatus(Status.warning);
			}
			bufferedElements.add(element);
		} else {
			testDetails.addReportElement(element);
			currentTest.setStatus(element.getStatus());
			if ((System.currentTimeMillis() - lastWrite) > config
					.getPropertyAsInt(DifidoOptions.MIN_TIME_BETWEEN_WRITES)) {
				lastWrite = System.currentTimeMillis();
				writeTestDetails(testDetails);
			}
		}
	}

	@Override
	public void log(String title, String message, Status status, ElementType type) {
		ReportElement element = new ReportElement();
		element = updateTimestampAndTitle(element, title);
		element.setMessage(message);
		element.setStatus(status);
		if (null == type) {
			type = ElementType.regular;
		}
		element.setType(type);
		log(element);
	}

	private ReportElement updateTimestampAndTitle(ReportElement element, String title) {
		element.setTime(REPORT_ELEMENT_TIME_FORMAT.format(new Date()));
		element.setTitle(title);
		return element;
	}

	private int getAllPlannedTestsCount(ISuite suite) {
		int totalPlanned = 0;

		for (ITestNGMethod method : suite.getAllMethods()) {
			totalPlanned += getDataProviderCases(method);
		}
		return totalPlanned;

	}

	/**
	 * This method counts all the planned test cases using dataProviders for the
	 * given ITestNGMethod.
	 * 
	 * @param method
	 * @return the number of cases, returns 1 if no dataProvider is found for
	 *         the test.
	 */
	private int getDataProviderCases(ITestNGMethod method) {
		int ret = 1;
		try {
			Method m = method.getConstructorOrMethod().getMethod();
			Object instance = method.getInstance();

			if (!m.isAnnotationPresent(Test.class)) {
				return ret;
			}

			String dataProviderName = m.getAnnotation(Test.class).dataProvider();
			if (null == dataProviderName || dataProviderName.isEmpty())
				return ret;

			Class<?> testClass = instance.getClass();
			for (Method classM : testClass.getMethods()) {
				if (classM.isAnnotationPresent(DataProvider.class)) {
					String thisDataProviderName = classM.getAnnotation(DataProvider.class).name();
					if (thisDataProviderName.equals(dataProviderName)) {
						try {
							Object[][] theData = (Object[][]) classM.invoke(instance);
							ret = theData.length;
							// System.out.printf("Found %s cases for %s\n", ret,
							// m.getName());
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
		if (inSetup || inTeardown) {
			bufferedTestProperties.put(name, value);
			return;
		}
		if (null == testDetails) {
			return;
		}
		currentTest.addProperty(name, value);
	}

	/**
	 * Add free property to the whole run
	 * 
	 * @param name
	 * @param value
	 */
	public void addRunProperty(String name, String value) {
		if (inSetup || inTeardown) {
			bufferedRunProperties.put(name, value);
			return;
		}
		if (null == currentClassScenario) {
			return;
		}
		log("Adding run property '" + name + "'='" + value + "'", null, Status.success, ElementType.regular);
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

	protected boolean isInSetup() {
		return inSetup;
	}

	protected boolean isInTeardown() {
		return inTeardown;
	}

}
