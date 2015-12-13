package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DifidoClient {

	private static final String BASE_URI_TEMPLATE = "http://%s:%d/api/";
	private final String baseUri;
	private final HttpClient client;

	public DifidoClient(String host, int port) {
		baseUri = String.format(BASE_URI_TEMPLATE, host, port);
		client = HttpClientBuilder.create().build();
	}

	public int addExecution(ExecutionDetails details) throws Exception {
		final HttpPost request = new HttpPost(baseUri + "executions/");
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		if (details != null){
			final String descriptionJson = new ObjectMapper().writeValueAsString(details);
			request.setEntity(new StringEntity(descriptionJson));
		}
		final HttpResponse response = client.execute(request);
		handleResponse(response);
		return Integer.parseInt(getResponseAsString(response));

	}
	
	public void endExecution(int executionId) throws Exception{
		final HttpPut request = new HttpPut(baseUri + "executions/" + executionId + "?active=false");
		request.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
		handleResponse(client.execute(request));
	}

	public int addMachine(int executionId, MachineNode machine) throws Exception {
		final HttpPost request = new HttpPost(baseUri + "executions/" + executionId + "/machines/");
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		final String machineJson = new ObjectMapper().writeValueAsString(machine);
		request.setEntity(new StringEntity(machineJson));
		final HttpResponse response = client.execute(request);
		handleResponse(response);
		return Integer.parseInt(getResponseAsString(response));
	}

	public void updateMachine(int executionId, int machineId, MachineNode machine) throws Exception {
		final HttpPut request = new HttpPut(baseUri + "executions/" + executionId + "/machines/" + machineId);
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		final String machineJson = new ObjectMapper().writeValueAsString(machine);
		request.setEntity(new StringEntity(machineJson));
		final HttpResponse response = client.execute(request);
		handleResponse(response);
	}

	public void addTestDetails(int executionId, TestDetails testDetails) throws Exception {
		final HttpPost request = new HttpPost(baseUri + "executions/" + executionId + "/details");
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		final String testDetailsJson = new ObjectMapper().writeValueAsString(testDetails);
		request.setEntity(new StringEntity(testDetailsJson));
		final HttpResponse response = client.execute(request);
		handleResponse(response);
	}

	public void addFile(int executionId, String uid, File file) throws Exception {
		final HttpPost request = new HttpPost(baseUri + "executions/" + executionId + "/details/" + uid + "/file/");
		HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("bin", new FileBody(file)).build();
		request.setEntity(reqEntity);
		final HttpResponse response = client.execute(request);
		handleResponse(response);
	}

	private String getResponseAsString(HttpResponse response) throws IllegalStateException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuilder result = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	private void handleResponse(final HttpResponse response) throws Exception {
		final int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode != 200 && responseCode != 204) {
			throw new Exception("Request was not successful. Response code is: " + responseCode + ".");
		}

	}

}
