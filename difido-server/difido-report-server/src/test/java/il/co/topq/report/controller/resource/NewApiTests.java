//package il.co.topq.report.controller.resource;
//
//import java.io.File;
//import java.util.Random;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import il.co.topq.difido.model.Enums.ElementType;
//import il.co.topq.difido.model.execution.MachineNode;
//import il.co.topq.difido.model.execution.ScenarioNode;
//import il.co.topq.difido.model.execution.TestNode;
//import il.co.topq.difido.model.test.ReportElement;
//import il.co.topq.difido.model.test.TestDetails;
//
//public class NewApiTests extends AbstractResourceTestCase {
//
//	private static final int NUM_OF_REPORTS_ELEMENTS = 50;
//	private DifidoClient client;
//	private String host = "localhost";
//	private int port = 8080;
//	private TestDetails details;
//	private int executionId;
//	private String uid;
//
//	@Before
//	public void setUp() throws Exception {
//		super.setUp();
//		client = new DifidoClient(host, port);
//		executionId = client.getLastExecutionId();
//		final MachineNode machine = new MachineNode("My machine");
//		final int machineId = client.addMachine(executionId, machine);
//		final ScenarioNode scenario = new ScenarioNode("My scenario 0");
//		machine.addChild(scenario);
//		final TestNode test = new TestNode(0, "My test","0");
//		uid = String.valueOf(Math.abs(new Random().nextInt()));
//		test.setUid(uid);
//		scenario.addChild(test);
//		client.updateMachine(executionId, machineId, machine);
//		details = new TestDetails("My Test Details","0");
//		details.setUid(uid);
//	}
//
//	@Test
//	public void testAddReportElement() throws Exception {
//		ReportElement element = new ReportElement(details);
//		element.setType(ElementType.regular);
//		element.setTime("00:00");
//		element.setTitle("My report element");
//		element.setMessage("My report element message");
//		details.addReportElement(element);
//		client.addTestDetails(executionId, details);
//	}
//
//	@Test
//	public void measureAddReportElements() throws Exception {
//		ReportElement element = null;
//		for (int i = 0; i < NUM_OF_REPORTS_ELEMENTS; i++) {
//			element = new ReportElement(details);
//			element.setType(ElementType.regular);
//			element.setTime("00:" + i);
//			element.setTitle("My report element " + i);
//			details.addReportElement(element);
//			long start = System.currentTimeMillis();
//			client.addTestDetails(executionId, details);
//			System.out.println("Element was added in " + (System.currentTimeMillis() - start) + " millis");
//		}
//
//	}
//
//	@Test
//	public void testAddFile() throws Exception {
//		final File file = new File("src/test/resources/top-q.pdf");
//		ReportElement element = new ReportElement(details);
//		element.setType(ElementType.lnk);
//		element.setTime("00:00");
//		element.setTitle("My report element");
//		element.setMessage(file.getName());
//		details.addReportElement(element);
//		client.addTestDetails(executionId, details);
//		client.addFile(executionId, uid, file);
//	}
//
//}
