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

public class JUnitXmlBinder extends DefaultHandler implements Binder {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private long baseTime;
	
	private long currentTime;
	
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
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(source, this);
	}
	
	@Override
	public void startDocument() throws SAXException {
		baseTime = System.currentTimeMillis();
		scenarioStack = new Stack<>();
		execution = new Execution();
		MachineNode machine = new MachineNode();
		machine.setName("JUnit Results");
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
		if (qName.equals("testsuite")) {
			startScenario(attributes.getValue("name"));
			return;
		}
		if (qName.equals("property")){
			startScenarioProperty(attributes);
			return;
			
		}
		if (qName.equals("testcase")){
			startTest(qName, attributes);
			return;
		}
		if (qName.equals("failure")){
			startFailure(attributes);
			return;
		}
		if (qName.equals("system-out")){
			startSystemOut();
			return;
		}
	}



	private void startFailure(Attributes attributes) {
		currentContent = new StringBuilder();
		currentTest.setStatus(Status.failure);
		currentElement = new ReportElement();
		currentElement.setStatus(Status.failure);
		currentElement.setTitle(attributes.getValue("message"));
		currentElement.setTime(TIME_FORMAT.format(currentTime));
		currentTestDetails.addReportElement(currentElement);
		
	}
	
	private void startSystemOut() {
		currentContent = new StringBuilder();
		currentElement = new ReportElement();
		currentElement.setTitle("System out");
		currentElement.setTime(TIME_FORMAT.format(currentTime));
		currentTestDetails.addReportElement(currentElement);
	}


	private void startScenarioProperty(Attributes attributes) {
		scenarioStack.peek().addScenarioProperty(attributes.getValue("name"), attributes.getValue("value"));
	}

	private void startTest(String qName, Attributes attributes) {
		currentTest = new TestNode(attributes.getValue("name"), ++id + "");
		final Map<String,String> properties = attributesToMap(attributes);
		if (null != properties.get("time")){
			currentTime = baseTime + (long)(1000 * Double.parseDouble(properties.get("time")));
			Date timeStamp = new Date(currentTime);
			currentTest.setDate(DATE_FORMAT.format(timeStamp));
			currentTest.setTimestamp(TIME_FORMAT.format(timeStamp));
		}
		currentTest.setIndex(id);
		currentTest.setProperties(properties);
		execution.getLastMachine().getChildren().get(0).addChild(currentTest);
		currentTestDetails = new TestDetails(id + "");
		testDetailsList.add(currentTestDetails);

	}

	private void startScenario(String name) {
		ScenarioNode scenario = new ScenarioNode(name);
		if (scenarioStack.isEmpty()){
			execution.getLastMachine().addChild(scenario);
			scenarioStack.push(scenario);
		} else {
			scenarioStack.peek().addChild(scenario);
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("testsuite")){
			endScenario(qName);
			return;
		}
		if (qName.equals("testcase")) {
			endTest();
			return;
		}
		if (qName.equals("failure")) {
			endFailure();
			return;
		}
		if (qName.equals("system-out")){
			endSystemOut();
			return;
		}
	}

	private void endSystemOut() {
		currentElement.setMessage(currentContent.toString());
		currentElement = null;
	}

	private void endFailure() {
		currentElement.setMessage(currentContent.toString());
		currentElement = null;
	}

	private void endScenario(String qName) {
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
