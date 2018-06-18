package il.co.topq.difido.binder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.difido.utils.Regex;

public class TrxBinder extends DefaultHandler implements Binder {

	private final static SimpleDateFormat TIME_MILLIS_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private Date zeroTime;
	
	private String currentTime;
	
	private Execution execution;

	private List<TestDetails> testDetailsList = new ArrayList<TestDetails>();

	private TestNode currentTest;
	
	private Stack<ScenarioNode> scenarioStack;

	private TestDetails currentTestDetails;
	
	private ReportElement currentElement;
	
	private StringBuilder currentContent;

	private int id;
	
	@Override
	public void process(File source) throws Exception {
		zeroTime = TIME_MILLIS_FORMAT.parse("00:00:00.000");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(source, this);
	}
	
	@Override
	public void startDocument() throws SAXException {
		scenarioStack = new Stack<>();
		execution = new Execution();
		MachineNode machine = new MachineNode();
		machine.setName("TRX Results");
		machine.setStatus(Status.success);
		execution.addMachine(machine);
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (null == currentContent || null == currentTestDetails){
			return;
		}
		final String content = new String(Arrays.copyOfRange(ch, start, start + length));
		if (content.replace("\n","").trim().isEmpty()) {
			return;
		}
		currentContent.append(content);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		// setMachineName
		if (qName.equals("TestRun")) {
			setMachineName(attributes.getValue("name"));
			return;
		}
		
		// startScenario
		if (qName.equals("Deployment")) {
			startScenario(attributes.getValue("userDeploymentRoot"));
			return;
		}
		
		// startTest
		if (qName.equals("UnitTestResult")){
			startTest(qName, attributes);
			return;
		}
		
		// startErrorMessage
		if (qName.equals("Message")) {
			startErrorMessage();
			return;
		}
		
		// startStackTrace
		if (qName.equals("StackTrace")) {
			startStackTrace();
			return;
		}
		
		// startStdOut
		if (qName.equals("StdOut")) {
			startStdOut();
			return;
		}
	}

	private void setMachineName(String testRunName) {
		String computerName = Regex.extractRegexGroup(testRunName, ".+@(.+)\\s\\d{4}-\\d{2}-\\d{2}.+", 1);
		execution.getLastMachine().setName(computerName);
	}

	private void startErrorMessage() {
		
		currentContent = new StringBuilder();

		currentElement = new ReportElement();
		currentElement.setStatus(Status.failure);
		currentElement.setTitle("Error Message");
		currentElement.setTime(currentTime);
		
		currentTestDetails.addReportElement(currentElement);
	}
	
	private void startStackTrace() {
		
		currentContent = new StringBuilder();

		currentElement = new ReportElement();
		currentElement.setStatus(Status.failure);
		currentElement.setTitle("Stack Trace");
		currentElement.setTime(currentTime);
		
		currentTestDetails.addReportElement(currentElement);
	}
	
	private void startStdOut() {
		
		currentContent = new StringBuilder();

		currentElement = new ReportElement();
		currentElement.setTitle("StdOut");
		currentElement.setTime(currentTime);
		
		currentTestDetails.addReportElement(currentElement);
	}
	
	private void startTest(String qName, Attributes attributes) {
		
		try {
			currentTest = new TestNode(attributes.getValue("testName"), ++id + "");
			
			// read all attributes as test properties
			final Map<String,String> properties = attributesToMap(attributes);
			currentTest.setProperties(properties);
			
			// test duration
			String durationStr = attributes.getValue("duration");
			durationStr = durationStr.substring(0, durationStr.lastIndexOf('.')+4);
			Date duration = TIME_MILLIS_FORMAT.parse(durationStr);
			long durationMillis = duration.getTime() - zeroTime.getTime();
			currentTest.setDuration(durationMillis);

			// test status
			String testOutcome = attributes.getValue("outcome");
			if (testOutcome.equals("Failed")) {
				currentTest.setStatus(Status.failure);
			}
			
			// date and time
			String startTimeStr = attributes.getValue("startTime");
			String timeStr = Regex.extractRegexGroup(startTimeStr, ".+(\\d{2}:\\d{2}:\\d{2}).+", 1);
			String dateStr = Regex.extractRegexGroup(startTimeStr, "(\\d{4}-\\d{2}-\\d{2}).+", 1);
			currentTest.setDate(dateStr);
			currentTest.setTimestamp(timeStr);
			currentTime = timeStr;
			
			currentTest.setIndex(id);
			execution.getLastMachine().getChildren().get(0).addChild(currentTest);
			currentTestDetails = new TestDetails(id + "");
			testDetailsList.add(currentTestDetails);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startScenario(String userDeploymentRoot) {
		
		String scenarioName = Regex.extractRegexGroup(userDeploymentRoot, ".+\\\\(.+)\\\\bin\\\\(Debug|Release)", 1);
		
		ScenarioNode scenario = new ScenarioNode(scenarioName);
		if (scenarioStack.isEmpty()){
			execution.getLastMachine().addChild(scenario);
			scenarioStack.push(scenario);
		} else {
			scenarioStack.peek().addChild(scenario);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equals("Message")){
			endErrorMessage();
			return;
		}
		
		if (qName.equals("StackTrace")){
			endStackTrace();
			return;
		}
		
		if (qName.equals("UnitTestResult")){
			endTest();
			return;
		}
		
		if (qName.equals("TestRun")) {
			endScenario();
			return;
		}
		
		if (qName.equals("StdOut")) {
			endStdOut();
			return;
		}
	}

	private void endErrorMessage() {
		currentElement.setMessage(currentContent.toString());
		currentElement = null;
	}
	
	private void endStackTrace() {
		currentElement.setMessage(currentContent.toString());
		currentElement = null;
	}
	
	private void endStdOut() {
		currentElement.setMessage(currentContent.toString());
		currentElement = null;
	}

	private void endScenario() {
		scenarioStack.pop();
	}

	private void endTest() {
		currentTest = null;
		currentTestDetails = null;
	}

	@Override
	public Execution getExecution() {
		return execution;
	}

	@Override
	public List<TestDetails> getTestDetails() {
		return testDetailsList;
	}
	
	private static Map<String, String> attributesToMap(Attributes attributes) {
		final Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < attributes.getLength(); i++) {
			map.put(attributes.getQName(i), attributes.getValue(i));
		}
		return map;
	}
}
