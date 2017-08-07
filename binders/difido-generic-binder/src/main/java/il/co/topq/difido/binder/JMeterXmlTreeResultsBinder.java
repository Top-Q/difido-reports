package il.co.topq.difido.binder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class JMeterXmlBinder extends DefaultHandler implements Binder {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private Execution execution;

	private List<TestDetails> testDetailsList = new ArrayList<TestDetails>();

	private TestNode currentTest;

	private TestDetails currentTestDetails;

	private ReportElement currentReportElement;
	
	private String currentTestName;
	
	private StringBuilder currentContent;

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
		String content = new String(Arrays.copyOfRange(ch, start, start + length));
		if (null == currentTestDetails || null == currentContent) {
			return;
		}
		if (content.replace("\n", "").trim().isEmpty()) {
			return;
		}
		currentContent.append(content);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("testResults")) {
			startScenario("Test Results: " + attributes.getValue("version"));
			return;
		}
		if (null == currentTestName){
			currentTestName = qName;
			startTest(qName,attributes);
			return;
		}
		startReportElement(qName);
		
	}


	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("testResults")) {
			return;
		}
		if (qName.equals(currentTestName)){
			currentTestName = null;
			endTest();
			return;
		}
		endReportElement();
	}
	
	private void endReportElement() {
		currentReportElement.setMessage(currentContent.toString());
		currentTestDetails.addReportElement(currentReportElement);
		currentReportElement = null;
	}

	private void startReportElement(String qName) {
		currentReportElement = new ReportElement();
		currentReportElement.setTitle(qName);
		currentReportElement.setTime(TIME_FORMAT.format(timeStamp));
		currentContent = new StringBuilder();
	}
	
	private void startTest(String qName, Attributes attributes) {
		currentTest = new TestNode(qName, ++id + "");
		currentTest.setIndex(id);
		final Map<String, String> properties = attributesToMap(attributes);
		if (null != properties.get("ts")) {
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


	private void startScenario(String name) {
		ScenarioNode testResults = new ScenarioNode(name);
		execution.getLastMachine().addChild(testResults);
	}


	private void endTest() {
		testDetailsList.add(currentTestDetails);
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
