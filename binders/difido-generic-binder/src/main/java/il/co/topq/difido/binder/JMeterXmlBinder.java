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

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

public class JMeterXmlBinder extends DefaultHandler implements Binder {
	
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	
	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	private Execution execution;

	private List<TestDetails> testDetailsList = new ArrayList<TestDetails>();

	private TestNode currentTest;

	private TestDetails currentTestDetails;

	private Stack<String> levelStack = new Stack<String>();
	
	private Date timeStamp;
	
	private int id;
	

	@Override
	public void process(File source) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(source, this);
	}

	@Override
	public void startDocument() throws SAXException {
		execution = new Execution();
		MachineNode machine = new MachineNode();
		machine.setName("JMeter Results");
		machine.setStatus(Status.success);
		execution.addMachine(machine);
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (null == currentTestDetails) {
			return;
		}
		ReportElement element = new ReportElement();
		element.setTime(TIME_FORMAT.format(timeStamp));
		String content = new String(Arrays.copyOfRange(ch, start, start + length));
		if (content.replace("\n","").trim().isEmpty()) {
			return;
		}
		element.setTitle(content);
		currentTestDetails.addReportElement(element);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("testResults")) {
			addRootScenario("Test Results: " + attributes.getValue("version"));
			return;
		}
		if (execution.getMachines().isEmpty() || execution.getLastMachine().getAllScenarios().isEmpty()) {
			// Before the test results
			return;
		}
		if (null == currentTest) {
			startTest(qName, attributes);
		} else {
			levelStack.add(qName);
			startLevel(qName, attributes);
		}
	}

	private void startLevel(String qName, Attributes attributes) {
		levelStack.add(qName);
		ReportElement element = new ReportElement();
		element.setTime(TIME_FORMAT.format(timeStamp));
		element.setType(ElementType.startLevel);
		element.setTitle(qName);
		currentTestDetails.addReportElement(element);

	}

	private void startTest(String qName, Attributes attributes) {
		currentTest = new TestNode(qName, ++id + "");
		currentTest.setIndex(id);
		final Map<String,String> properties = attributesToMap(attributes);
		if (null != properties.get("ts")){
			timeStamp = new Date(Long.parseLong(properties.get("ts")));
			currentTest.setDate(DATE_FORMAT.format(timeStamp));
			currentTest.setTimestamp(TIME_FORMAT.format(timeStamp));
		} else {
			timeStamp = null;
		}
		currentTest.setProperties(properties);
		execution.getLastMachine().getChildren().get(0).addChild(currentTest);
		currentTestDetails = new TestDetails(id + "");

	}

	private Map<String, String> attributesToMap(Attributes attributes) {
		final Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < attributes.getLength(); i++) {
			map.put(attributes.getQName(i), attributes.getValue(i));
		}
		return map;
	}

	private void addRootScenario(String name) {
		ScenarioNode testResults = new ScenarioNode(name);
		execution.getLastMachine().addChild(testResults);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentTest == null || qName.equals(currentTest.getName())) {
			endTest();
			return;
		}
		if (!levelStack.isEmpty()) {
			levelStack.pop();
			endLevel();
		}
		
	}

	private void endTest() {
		testDetailsList.add(currentTestDetails);
		currentTest = null;
		currentTestDetails = null;
		levelStack.clear();
	}

	private void endLevel() {
		ReportElement element = new ReportElement();
		element.setTime(TIME_FORMAT.format(timeStamp));
		element = new ReportElement();
		element.setType(ElementType.stopLevel);
		currentTestDetails.addReportElement(element);
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
