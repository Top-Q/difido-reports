package il.co.topq.difido.client;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

public class DifidoClient {

	private final WebTarget baseTarget;

	private DifidoClient(String baseUri) {
		Client client = ClientBuilder.newClient();
		client.register(JacksonObjectMapper.class);
		client.register(MultiPartWriter.class);
		this.baseTarget = client.target(baseUri);
	}

	public static DifidoClient build(String baseUri) {
		return new DifidoClient(baseUri);
	}

	/**
	 * invoke: ExecutionResource.post()
	 */
	public int addExecution() {
		WebTarget executionsTarget = baseTarget.path("/executions");
		Response response = executionsTarget.request(MediaType.TEXT_PLAIN).post(null);
		int executionId = Integer.parseInt(response.readEntity(String.class));
		return executionId;
	}

	public void endExecution(int executionId) {
		WebTarget executionsTarget = baseTarget.path("/executions/" + executionId);
		executionsTarget.request(MediaType.TEXT_PLAIN).delete();
	}

	/**
	 * invoke: MachineResource.post()
	 */
	public int addMachine(int executionId, MachineNode machine) {
		WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines");
		Response postResponse = machinesTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(machine, MediaType.APPLICATION_JSON));
		int machineId = Integer.parseInt(postResponse.readEntity(String.class));
		return machineId;
	}

	/**
	 * invoke: MachineResource.get()
	 */
	public MachineNode getMachine(int executionId, int machineId) {
		WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId);
		MachineNode machine = machinesTarget.request(MediaType.APPLICATION_JSON).get(MachineNode.class);
		return machine;
	}

	public int addRootScenario(int executionId, int machineId, ScenarioNode scenario) {
		WebTarget scenarioTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios");
		Response postResponse = scenarioTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(scenario, MediaType.APPLICATION_JSON));
		int scenarioId = Integer.parseInt(postResponse.readEntity(String.class));
		return scenarioId;
	}

	public int addSubScenario(int executionId, int machineId, int parentScenarioId, ScenarioNode scenario) {
		WebTarget scenarioTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + parentScenarioId);
		Response postResponse = scenarioTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(scenario, MediaType.APPLICATION_JSON));
		int scenarioId = Integer.parseInt(postResponse.readEntity(String.class));
		return scenarioId;
	}

	public int addTest(int executionId, int machineId, int scenarioId, TestNode test) {
		WebTarget testTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests");
		Response postResponse = testTarget.request(MediaType.TEXT_PLAIN).post(
				Entity.entity(test, MediaType.APPLICATION_JSON));
		int testId = Integer.parseInt(postResponse.readEntity(String.class));
		return testId;
	}

	public TestNode getTest(int executionId, int machineId, int scenarioId, int testId) {
		WebTarget testTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests/" + testId);
		TestNode test = testTarget.request(MediaType.APPLICATION_JSON).get(TestNode.class);
		return test;
	}

	public void updateTest(int executionId, int machineId, int scenarioId, int testId, TestNode test) {
		WebTarget testTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests/" + testId);
		testTarget.request().put(Entity.entity(test, MediaType.APPLICATION_JSON));
	}

	public void addTestDetails(int executionId, int machineId, int scenarioId, int testId, TestDetails details) {
		WebTarget testDetailsTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details");
		testDetailsTarget.request().post(Entity.entity(details, MediaType.APPLICATION_JSON));
	}

	public TestDetails getTestDetails(int executionId, int machineId, int scenarioId, int testId) {
		WebTarget testDetailsTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details");
		TestDetails details = testDetailsTarget.request(MediaType.APPLICATION_JSON).get(TestDetails.class);
		return details;
	}

	public void addReportElement(int executionId, int machineId, int scenarioId, int testId, ReportElement element) {
		WebTarget elementTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId + "/tests/" + testId + "/details/element");
		elementTarget.request().post(Entity.entity(element, MediaType.APPLICATION_JSON));
	}

	public ScenarioNode getScenario(int executionId, int machineId, int scenarioId) {
		WebTarget scenariosTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId
				+ "/scenarios/" + scenarioId);
		ScenarioNode scenario = scenariosTarget.request(MediaType.APPLICATION_JSON).get(ScenarioNode.class);
		return scenario;
	}
	
	public String uploadFile(File uploadedFile, String destinationDirRelativePath) {
		
		FormDataMultiPart multiPart = new FormDataMultiPart();
	    multiPart.bodyPart(new FileDataBodyPart("file", uploadedFile, MediaType.APPLICATION_OCTET_STREAM_TYPE));
	    multiPart.field("destinationDirRelativePath", destinationDirRelativePath);
		
		WebTarget uploadTarget = baseTarget.path("/upload/file");
		Response response = uploadTarget.request(MediaType.TEXT_PLAIN).post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA));

		return response.readEntity(String.class);
	}
}
