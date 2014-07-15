package il.co.topq.report.resource;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Main;
import il.co.topq.report.model.Session;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractResourceTestCase {

	private HttpServer server;
	private WebTarget baseTarget;

	@Before
	public void setUp() throws Exception {
		server = Main.startServer();
		Client client = ClientBuilder.newClient();
		baseTarget = client.target(Main.BASE_URI);
		System.out.println("@Before - Grizzly server started on: " + Main.BASE_URI);
		Session.INSTANCE.flush();
	}

	@After
	public void tearDown() throws Exception {
		server.shutdownNow();
		System.out.println("\n@After - Grizzly server shut down");
		System.out.println(Session.INSTANCE.getExecution());
	}

	/**
	 * invoke: ExecutionResource.post()
	 */
	protected int addExecution() {
		WebTarget executionsTarget = baseTarget.path("/executions");

		System.out.println("\nAdding new execution -" + "\nPOST request to: " + executionsTarget.getUri());

		Response response = executionsTarget.request(MediaType.TEXT_PLAIN).post(null);
		int executionId = Integer.parseInt(response.readEntity(String.class));

		System.out.println(">> Received execution ID: " + executionId);
		return executionId;
	}

	/**
	 * invoke: MachineResource.post()
	 */
	protected int addMachine(int executionId, String machineName) {
		WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines");

		System.out.println("\nAdding new machine to execution <" + executionId + "> -" + "\nPOST request to: "
				+ machinesTarget.getUri());

		Response postResponse = machinesTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(new MachineNode(machineName), MediaType.APPLICATION_JSON));
		int machineId = Integer.parseInt(postResponse.readEntity(String.class));

		System.out.println(">> Received machine ID: " + machineId);
		return machineId;
	}

	protected int addRootScenario(int executionId, int machineId, String scenarioName) {
		WebTarget scenarioTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios");

		System.out.println("\nAdding new scenario to execution <" + executionId + "> and machine " + machineId + " -"
				+ "\nPOST request to: " + scenarioTarget.getUri());

		Response postResponse = scenarioTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(new ScenarioNode(scenarioName), MediaType.APPLICATION_JSON));
		int scenarioId = Integer.parseInt(postResponse.readEntity(String.class));

		System.out.println(">> Received scenario ID: " + scenarioId);
		return scenarioId;
	}

	protected int addSubScenario(int executionId, int machineId, int parentScenarioId, String scenarioName) {
		WebTarget scenarioTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + parentScenarioId);

		System.out.println("\nAdding new scenario to execution <" + executionId + "> and machine <" + machineId
				+ "> and scenario <" + parentScenarioId + "> -" + "\nPOST request to: " + scenarioTarget.getUri());

		Response postResponse = scenarioTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(new ScenarioNode(scenarioName), MediaType.APPLICATION_JSON));
		int scenarioId = Integer.parseInt(postResponse.readEntity(String.class));

		System.out.println(">> Received scenario ID: " + scenarioId);
		return scenarioId;
	}

	protected int addTest(int executionId, int machineId, int scenarioId, String testName) {
		WebTarget testTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests");

		System.out.println("\nAdding new test to execution <" + executionId + "> and machine " + machineId
				+ " and scenario <" + scenarioId + ">-" + "\nPOST request to: " + testTarget.getUri());

		Response postResponse = testTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(new TestNode(testName), MediaType.APPLICATION_JSON));
		int testId = Integer.parseInt(postResponse.readEntity(String.class));

		System.out.println(">> Received test ID: " + testId);
		return testId;
	}

	protected TestNode getTest(int executionId, int machineId, int scenarioId, int testId) {
		WebTarget testTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests/" + testId);

		System.out.println("\nGetting test <" + testId + "> from scenario <" + scenarioId + "> from machine <"
				+ machineId + "> from execution <" + executionId + ">-" + "\nGET request to: " + testTarget.getUri());

		TestNode test = testTarget.request(MediaType.APPLICATION_JSON).get(TestNode.class);

		System.out.println(">> Received test with name: \"" + test.getName() + "\"");
		return test;
	}

	protected void addTestDetails(int executionId, int machineId, int scenarioId, int testId, TestDetails details) {
		WebTarget testDetailsTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details");
		System.out.println("\nAdding new test details in execution <" + executionId + "> and machine " + machineId
				+ " and scenario <" + scenarioId + ">-" + "\nPOST request to: " + testDetailsTarget.getUri());
		testDetailsTarget.request().post(Entity.entity(details, MediaType.APPLICATION_JSON));
	}

	protected TestDetails getTestDetails(int executionId, int machineId, int scenarioId, int testId) {
		WebTarget testDetailsTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details");
		System.out.println("\nGetting test details in execution <" + executionId + "> and machine " + machineId
				+ " and scenario <" + scenarioId + ">-" + "\nGET request to: " + testDetailsTarget.getUri());
		TestDetails details = testDetailsTarget.request(MediaType.APPLICATION_JSON).get(TestDetails.class);
		System.out.println(">> Revcieved test details: " + details);
		return details;
	}

	protected void addReportElement(int executionId, int machineId, int scenarioId, int testId, ReportElement element) {
		WebTarget elementTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details/element");
		System.out.println("\nAdding new report element in execution <" + executionId + "> and machine " + machineId
				+ " and scenario <" + scenarioId + ">-" + "\nPOST request to: " + elementTarget.getUri());
		elementTarget.request().post(Entity.entity(element, MediaType.APPLICATION_JSON));
	}

//	protected ReportElement[] getReportElements(int executionId, int machineId, int scenarioId, int testId) {
//		WebTarget elementTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
//				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details/element");
//		ReportElement[] elements = elementTarget.request(MediaType.APPLICATION_JSON).get(ReportElement[].class);
//		System.out.println(">> Recieved report elements "+ Arrays.toString(elements));
//		return elements;
//
//	}

	protected void updateTest(int executionId, int machineId, int scenarioId, int testId, TestNode test) {
		WebTarget testTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests/" + testId);

		System.out.println("\nUpdating test in execution <" + executionId + "> and machine " + machineId
				+ " and scenario <" + scenarioId + ">-" + "\nPOST request to: " + testTarget.getUri());

		testTarget.request().put(Entity.entity(test, MediaType.APPLICATION_JSON));
	}

	protected ScenarioNode getScenario(int executionId, int machineId, int scenarioId) {
		WebTarget scenariosTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId);

		System.out.println("\nGetting scenario <" + scenarioId + "> from machine <" + machineId + "> from execution <"
				+ executionId + ">-" + "\nGET request to: " + scenariosTarget.getUri());

		ScenarioNode scenario = scenariosTarget.request(MediaType.APPLICATION_JSON).get(ScenarioNode.class);

		System.out.println(">> Received scenario with name: \"" + scenario.getName() + "\"");
		return scenario;
	}

	/**
	 * invoke: MachineResource.get()
	 */
	protected MachineNode getMachine(int executionId, int machineId) {
		WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId);

		System.out.println("\nGetting machine <" + machineId + "> from execution <" + executionId + "> -"
				+ "\nGET request to: " + machinesTarget.getUri());

		MachineNode machine = machinesTarget.request(MediaType.APPLICATION_JSON).get(MachineNode.class);

		System.out.println(">> Received machine with name: \"" + machine.getName() + "\"");
		return machine;
	}
}
