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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class JMeterXmlTreeResultsBinder extends DefaultHandler implements Binder {

	private Logger log = LoggerFactory.getLogger(JMeterXmlTreeResultsBinder.class);

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private Execution execution;

	private List<TestDetails> testDetailsList = new ArrayList<TestDetails>();

	private TestNode currentTest;

	private TestDetails currentTestDetails;

	private String currentTestName;

	private Stack<StringBuilder> contentStack;

	private Stack<ReportElement> elementsStack;

	private Date timeStamp;

	private int id;

	private int sampleLevel;
	
	@Override
	public void process(File source) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(source, this);
	}

	@Override
	public void startDocument() throws SAXException {
		contentStack = new Stack<>();
		elementsStack = new Stack<>();
		execution = new Execution();
		MachineNode machine = new MachineNode();
		machine.setName("JMeter Results");
		machine.setStatus(Status.success);
		execution.addMachine(machine);
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		String content = new String(Arrays.copyOfRange(ch, start, start + length));
		if (null == currentTestDetails || contentStack.isEmpty()) {
			return;
		}
		if (content.replace("\n", "").trim().isEmpty()) {
			return;
		}
		contentStack.peek().append(content);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("testResults")) {
			startScenario("Test Results: " + attributes.getValue("version"));
			return;
		}
		if (qName.equals("httpSample")) {
			sampleLevel++;
			if (null == currentTestName) {
				currentTestName = qName;
				startTest(qName, new HttpSampleAttibutes(attributes));
			} else {
				startLevel(qName, new HttpSampleAttibutes(attributes));

			}
			return;
		}
		startReportElementWithMessage(qName);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("testResults")) {
			return;
		}
		if (qName.equals(currentTestName)) {
			if (--sampleLevel == 0) {
				currentTestName = null;
				endTest();

			} else {
				endLevel();
			}
			return;
		}
		endReportElementWithMessage();
	}


	private void startReportElementWithMessage(String qName) {
		log.debug("Starting report element " + qName + " with message");
		ReportElement element = new ReportElement();
		element.setType(ElementType.regular);
		element.setTitle(qName);
		element.setTime(TIME_FORMAT.format(timeStamp));
		elementsStack.push(element);
		contentStack.push(new StringBuilder());
	}
	
	private void endReportElementWithMessage() {
		log.debug("Ending report element with message");
		ReportElement element = elementsStack.pop();
		String content = contentStack.pop().toString();
		content = content.replace("<", "&lt;").replace(">", "&gt;");
		element.setMessage(content);
		currentTestDetails.addReportElement(element);
	}


	private void startTest(String qName, HttpSampleAttibutes attributes) {
		log.debug("Starting test " + attributes.getLabel());
		currentTest = new TestNode(attributes.getLabel(), ++id + "");
		currentTest.setIndex(id);
		currentTest.setStatus(attributes.isStatus() ? Status.success : Status.failure);
		final Map<String, String> properties = attributesToMap(attributes.getAttributes());
		timeStamp = attributes.getTimestamp();
		currentTest.setDate(DATE_FORMAT.format(timeStamp));
		currentTest.setTimestamp(TIME_FORMAT.format(timeStamp));
		currentTest.setProperties(properties);
		execution.getLastMachine().getChildren().get(0).addChild(currentTest);
		currentTestDetails = new TestDetails(id + "");

		ReportElement element = new ReportElement();
		element.setTime(TIME_FORMAT.format(timeStamp));
		element.setTitle("Return code: " + attributes.getReturnCode());
		currentTestDetails.addReportElement(element);

		element = new ReportElement();
		element.setTime(TIME_FORMAT.format(timeStamp));
		element.setTitle("Return message: " + attributes.getReturnMessage());
		currentTestDetails.addReportElement(element);

		element = new ReportElement();
		element.setTime(TIME_FORMAT.format(timeStamp));
		element.setTitle("Thread group: " + attributes.getThreadGroup());
		currentTestDetails.addReportElement(element);

	}

	private void startLevel(String qName, HttpSampleAttibutes attributes) {
		log.debug("Starting level");
		ReportElement element = new ReportElement();
		String time = TIME_FORMAT.format(attributes.getTimestamp());
		element.setTime(time);
		element.setTitle(attributes.getLabel());
		element.setType(ElementType.startLevel);
		element.setStatus(attributes.isStatus() ? Status.success : Status.failure);
		currentTestDetails.addReportElement(element);

		element = new ReportElement();
		element.setTime(time);
		element.setTitle("Return code: " + attributes.getReturnCode());
		currentTestDetails.addReportElement(element);

		element = new ReportElement();
		element.setTime(time);
		element.setTitle("Return message: " + attributes.getReturnMessage());
		currentTestDetails.addReportElement(element);

		element = new ReportElement();
		element.setTime(time);
		element.setTitle("Thread group: " + attributes.getThreadGroup());
		currentTestDetails.addReportElement(element);
	}

	private void endLevel() {
		log.debug("Ending level");
		ReportElement element = new ReportElement();
		element.setTitle("----");
		element.setTime("----");
		element.setType(ElementType.stopLevel);
		currentTestDetails.addReportElement(element);
	}

	private void startScenario(String name) {
		log.debug("Starting scenario " + name);
		ScenarioNode testResults = new ScenarioNode(name);
		execution.getLastMachine().addChild(testResults);
	}

	private void endTest() {
		log.debug("Ending test");
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

	class HttpSampleAttibutes {

		private final Attributes attributes;

		private final String label;

		private final boolean status;

		private final Date timestamp;

		private final int returnCode;

		private final String returnMessage;

		private final String threadGroup;

		public HttpSampleAttibutes(Attributes attributes) {
			super();
			this.attributes = attributes;
			label = attributes.getValue("lb");
			status = Boolean.parseBoolean(attributes.getValue("s"));
			timestamp = new Date(Long.parseLong(attributes.getValue("ts")));
			returnCode = Integer.parseInt(attributes.getValue("rc"));
			returnMessage = attributes.getValue("rm");
			threadGroup = attributes.getValue("tn");
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("label", getLabel());
			map.put("status", isStatus());
			map.put("timestamp", getTimestamp());
			map.put("return code", getReturnCode());
			map.put("return message", getReturnMessage());
			map.put("thread group", getThreadGroup());
			return map;
		}

		public Attributes getAttributes() {
			return attributes;
		}

		public String getLabel() {
			return label;
		}

		public boolean isStatus() {
			return status;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public int getReturnCode() {
			return returnCode;
		}

		public String getReturnMessage() {
			return returnMessage;
		}

		public String getThreadGroup() {
			return threadGroup;
		}

	}

}
