package il.co.topq.difido.client;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.test.TestDetails;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

public class DifidoClient {

	private final WebTarget baseTarget;

	private DifidoClient(String baseUri) {
		Client client = ClientBuilder.newClient();
		client.register(JacksonFeature.class);
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

	public int getLastExecutionId() {
		WebTarget executionsTarget = baseTarget.path("/executions/lastId");
		String response = executionsTarget.request(MediaType.TEXT_PLAIN).get(String.class);
		int executionId = Integer.parseInt(response);
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

	public void updateMachine(int executionId, int machineId, MachineNode machine) throws Exception {
		WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId);
		Response response = machinesTarget.request(MediaType.TEXT_PLAIN).put(
				Entity.entity(machine, MediaType.APPLICATION_JSON));
		handleSimpleRespose(response);
	}

	public void addTestDetails(int executionId, TestDetails details) throws Exception {
		WebTarget testDetailsTarget = baseTarget.path("/executions/" + executionId + "/details");
		Response response = testDetailsTarget.request().post(Entity.entity(details, MediaType.APPLICATION_JSON));
		handleSimpleRespose(response);
	}

	public void addFile(int executionId, int machineId, int scenarioId, int testId, File uploadedFile) {
		WebTarget fileTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId + "/scenarios/"
				+ scenarioId + "/tests/" + testId + "/details/file");

		FormDataMultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new FileDataBodyPart("file", uploadedFile, MediaType.APPLICATION_OCTET_STREAM_TYPE));

		fileTarget.request(MediaType.TEXT_PLAIN).post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA));
	}

	private void handleSimpleRespose(final Response response) throws Exception {
		if (response.getStatus() != 202 && response.getStatus() != 204) {
			throw new Exception("Request failed - status " + response.getStatus());
		}
	}

	public void close() {

	}
}
