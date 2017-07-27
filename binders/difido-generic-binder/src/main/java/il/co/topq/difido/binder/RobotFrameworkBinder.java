package il.co.topq.difido.binder;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

public class RobotFrameworkBinder extends DefaultHandler implements Binder {

	private Logger log = LoggerFactory.getLogger(RobotFrameworkBinder.class);

	private static final int MAX_NAME_SIZE = 140;

	private static final boolean SKIP_FIRST_SCENARIO = true;

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	
	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	// 20170726 17:02:33.917
	private final static SimpleDateFormat ROBOT_TIMESTAMP = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");

	private boolean firstScenario = true;

	private boolean finished;

	private Execution execution;

	private RobotStatus lastStatus;

	private List<TestDetails> testDetailsList = new ArrayList<TestDetails>();

	private Stack<Node> nodeStack;

	private Stack<ScenarioNode> scenarioStack;

	private TestDetails currentTestDetails;

	private Stack<ReportElement> keyWordsStack;

	private StringBuilder currentContent;

	private List<ReportElement> orphanElements;

	private int propId;

	private int uid;

	@Override
	public void process(File source) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(source, this);
	}

	@Override
	public void startDocument() throws SAXException {
		scenarioStack = new Stack<>();
		nodeStack = new Stack<>();
		execution = new Execution();
		keyWordsStack = new Stack<>();
		orphanElements = new ArrayList<ReportElement>();
		currentContent = new StringBuilder();
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		final String content = new String(Arrays.copyOfRange(ch, start, start + length));
		if (content.replace("\n", "").trim().isEmpty()) {
			return;
		}
		currentContent.append(content);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (finished) {
			return;
		}
		log.debug("Starting element from type: " + qName);
		if (qName.equals("robot")) {
			startRobot(attributes.getValue("generated"), attributes.getValue("generator"));
			return;
		}
		if (qName.equals("suite")) {
			startSuite(attributes.getValue("source"), attributes.getValue("id"), attributes.getValue("name"));
			return;
		}
		if (qName.equals("test")) {
			startTest(attributes.getValue("id"), attributes.getValue("name"));
			return;
		}
		if (qName.equals("kw")) {
			startKeyword(attributes.getValue("name"));
			return;
		}
		if (qName.equals("arg")) {
			startArgument();
			return;
		}
		if (qName.equals("status")) {
			startStatus(attributes.getValue("status"), attributes.getValue("endtime"),
					attributes.getValue("starttime"));
			return;
		}
		if (qName.equals("msg")) {
			startMessage(attributes.getValue("timestamp"), attributes.getValue("level"));
			return;
		}
		if (qName.equals("doc")) {
			startDoc();
			return;
		}
		if (qName.equals("tag")) {
			startTag();
			return;
		}
		if (qName.equals("timeout")) {
			startTimeout(attributes.getValue("value"));
			return;
		}

		if (qName.equals("statistics")) {
			finished = true;
			return;
		}
	}

	private void startTimeout(String timeout) {
		if (!nodeStack.isEmpty() && nodeStack.peek() instanceof TestNode) {
			((TestNode) nodeStack.peek()).addProperty("timeout", timeout);
		}
	}

	private void startTag() {
		currentContent = new StringBuilder();
	}

	private void startArgument() {
		currentContent = new StringBuilder();

	}

	private void startDoc() {
		log.debug("Starting doc");
		currentContent = new StringBuilder();
	}

	private void startMessage(String timestamp, String level) {
		log.debug("Starting message");
		try {
			if (timestamp != null) {
				lastStatus = new RobotStatus("PASS", ROBOT_TIMESTAMP.parse(timestamp),
						ROBOT_TIMESTAMP.parse(timestamp));
			}
		} catch (ParseException e) {
			log.error("Failed to parse date");
		}
		currentContent = new StringBuilder();
	}

	private void startStatus(String status, String endTime, String startTime) {
		log.debug("Starting status");
		try {
			lastStatus = new RobotStatus(status, ROBOT_TIMESTAMP.parse(startTime), ROBOT_TIMESTAMP.parse(startTime));
		} catch (ParseException e) {
			log.error("Failed to parse date");
		}
	}

	private void startKeyword(String name) {
		log.debug("Starting keyword");
		ReportElement kw = new ReportElement();
		kw.setTitle(name);
		kw.setType(ElementType.startLevel);
		if (null == currentTestDetails) {
			orphanElements.add(kw);
		} else {
			currentTestDetails.addReportElement(kw);
		}
		keyWordsStack.push(kw);

	}

	private void startTest(String id, String name) {
		log.debug("Starting test");
		TestNode test = new TestNode(name, ++uid + "");
		test.setIndex(uid);
		test.addProperty("id", id);
		nodeStack.push(test);
		scenarioStack.peek().addChild(test);
		currentTestDetails = new TestDetails(Integer.toString(uid));
		for (ReportElement element : orphanElements) {
			currentTestDetails.addReportElement(element);
		}
		orphanElements.clear();
	}

	private void startSuite(String source, String id, String name) {
		log.debug("Starting suite");
		if (SKIP_FIRST_SCENARIO && firstScenario) {
			firstScenario = false;
			return;
		}
		if (name != null && name.length() > MAX_NAME_SIZE) {
			name = name.substring(0, MAX_NAME_SIZE);
		}
		ScenarioNode scenario = new ScenarioNode(name);
		if (scenarioStack.isEmpty()) {
			execution.getLastMachine().addChild(scenario);
		} else {
			scenarioStack.peek().addChild(scenario);
		}
		scenarioStack.push(scenario);
		nodeStack.push(scenario);
	}

	private void startRobot(String generated, String generation) {
		log.debug("Starting robot");
		MachineNode machine = new MachineNode();
		machine.setName(generation);
		machine.setStatus(Status.success);
		execution.addMachine(machine);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (finished) {
			return;
		}
		log.debug("Ending element from type: " + qName);
		if (qName.equals("suite")) {
			endSuite();
			return;
		}
		if (qName.equals("test")) {
			endTest();
			return;
		}
		if (qName.equals("kw")) {
			endKeyword();
			return;
		}
		if (qName.equals("arg")) {
			endArgument();
			return;
		}
		if (qName.equals("msg")) {
			endMessage();
			return;
		}
		if (qName.equals("doc")) {
			endDoc();
			return;
		}
		if (qName.equals("tag")) {
			endTag();
			return;
		}

	}

	private void endTag() {
		((TestNode) nodeStack.peek()).addProperty("tag" + propId++, currentContent.toString());
		;
	}

	private void endArgument() {
		log.debug("Ending argument");
		ReportElement element = new ReportElement();
		if (lastStatus != null) {
			element.setTime(TIME_FORMAT.format(lastStatus.getStartTime()));
		} else {
			element.setTime("----");
		}
		element.setTitle(currentContent.toString());
		if (null == currentTestDetails) {
			orphanElements.add(element);
		} else {
			currentTestDetails.addReportElement(element);
		}
		if (nodeStack.peek() instanceof TestNode) {
			((TestNode) nodeStack.peek()).addParameter("param" + propId++, currentContent.toString());
		}

	}

	private void endDoc() {
		log.debug("Ending doc");
		ReportElement element = new ReportElement();
		element.setTitle("Documentation");
		element.setMessage(currentContent.toString());
		if (lastStatus != null) {
			element.setTime(TIME_FORMAT.format(lastStatus.getStartTime()));
		}
		if (keyWordsStack.isEmpty() && nodeStack.peek() instanceof TestNode) {
			((TestNode) nodeStack.peek()).setDescription(currentContent.toString());
		} else {
			if (currentTestDetails != null) {
				currentTestDetails.addReportElement(element);
			} else {
				orphanElements.add(element);
			}

		}

	}

	private void endMessage() {
		log.debug("Ending message");
		ReportElement element = new ReportElement();
		element.setTitle(currentContent.toString());
		if (lastStatus != null) {
			element.setTime(TIME_FORMAT.format(lastStatus.getStartTime()));
			element.setStatus(parseStatus(lastStatus.getStatus()));
		}
		if (null == currentTestDetails) {
			orphanElements.add(element);
		} else {
			currentTestDetails.addReportElement(element);

		}
	}

	private void endKeyword() {
		log.debug("Ending keyword");
		ReportElement kw = new ReportElement();
		kw.setType(ElementType.stopLevel);
		kw.setStatus(parseStatus(lastStatus.getStatus()));
		if (currentTestDetails == null) {
			orphanElements.add(kw);
		} else {
			currentTestDetails.addReportElement(kw);
		}

		ReportElement startLevel = keyWordsStack.pop();
		startLevel.setTime(TIME_FORMAT.format(lastStatus.getStartTime()));
		startLevel.setStatus(parseStatus(lastStatus.getStatus()));
	}

	private void endSuite() {
		log.debug("Ending suite");
		if (scenarioStack.isEmpty()) {
			return;
		}
		nodeStack.pop();
		scenarioStack.pop();
	}

	private Status parseStatus(String status) {
		if (status == null) {
			log.error("Recieved null stats");
			return Status.success;
		}
		if (status.equals("PASS")) {
			return Status.success;
		}
		return Status.failure;
	}

	private void endTest() {
		log.debug("Ending test");
		testDetailsList.add(currentTestDetails);
		currentTestDetails = null;
		TestNode test = (TestNode) nodeStack.pop();
		test.setDate(DATE_FORMAT.format(lastStatus.getStartTime()));
		test.setTimestamp(TIME_FORMAT.format(lastStatus.getStartTime()));
		test.setStatus(parseStatus(lastStatus.getStatus()));
		propId = 0;
	}


	@Override
	public Execution getExecution() {
		return execution;
	}

	@Override
	public List<TestDetails> getTestDetails() {
		return testDetailsList;
	}

	class RobotStatus {

		private String status;

		private Date startTime;

		private Date endTime;

		public RobotStatus(String status, Date startTime, Date endTime) {
			super();
			this.status = status;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}

	}

}
