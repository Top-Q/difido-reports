//package il.co.topq.report.controller.resource;
//
//import il.co.topq.difido.model.execution.MachineNode;
//import il.co.topq.difido.model.test.TestDetails;
//
//import java.io.File;
//
//import javax.ws.rs.core.HttpHeaders;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//public class DifidoClient {
//
//	private static final String BASE_URI_TEMPLATE = "http://%s:%d/api/";
//	private final String baseUri;
//	private final HttpClient client;
//
//	public DifidoClient(String host, int port) {
//		baseUri = String.format(BASE_URI_TEMPLATE, host, port);
//		client = new HttpClient();
//	}
//
//	public int addExecution() throws Exception {
//		final PostMethod method = new PostMethod(baseUri + "executions/");
//		method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE, "text/plain"));
//		final int responseCode = client.executeMethod(method);
//		handleResponseCode(method, responseCode);
//		return Integer.parseInt(method.getResponseBodyAsString());
//
//	}
//
//	public int getLastExecutionId() throws Exception {
//		final GetMethod method = new GetMethod(baseUri + "executions/lastId");
//		method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE, "text/plain"));
//		final int responseCode = client.executeMethod(method);
//		handleResponseCode(method, responseCode);
//		return Integer.parseInt(method.getResponseBodyAsString());
//	}
//
//	public int addMachine(int executionId, MachineNode machine) throws Exception {
//		PostMethod method = new PostMethod(baseUri + "executions/" + executionId + "/machines/");
//		method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE, "application/json"));
//		final ObjectMapper mapper = new ObjectMapper();
//		final String json = mapper.writeValueAsString(machine);
//		final RequestEntity entity = new StringRequestEntity(json);
//		method.setRequestEntity(entity);
//		int responseCode = client.executeMethod(method);
//		handleResponseCode(method, responseCode);
//		return Integer.parseInt(method.getResponseBodyAsString());
//	}
//
//	public void updateMachine(int executionId, int machineId, MachineNode machine) throws Exception {
//		PutMethod method = new PutMethod(baseUri + "executions/" + executionId + "/machines/" + machineId);
//		method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE, "application/json"));
//		final ObjectMapper mapper = new ObjectMapper();
//		final String json = mapper.writeValueAsString(machine);
//		final RequestEntity entity = new StringRequestEntity(json);
//		method.setRequestEntity(entity);
//		int responseCode = client.executeMethod(method);
//		handleResponseCode(method, responseCode);
//	}
//
//	public void addTestDetails(int executionId, TestDetails testDetails) throws Exception {
//		PostMethod method = new PostMethod(baseUri + "executions/" + executionId + "/details");
//		method.setRequestHeader(new Header(HttpHeaders.CONTENT_TYPE, "application/json"));
//		final ObjectMapper mapper = new ObjectMapper();
//		final String json = mapper.writeValueAsString(testDetails);
//		final RequestEntity entity = new StringRequestEntity(json);
//		method.setRequestEntity(entity);
//		final int responseCode = client.executeMethod(method);
//		handleResponseCode(method, responseCode);
//	}
//
//	public void addFile(int executionId, String uid, File file) throws Exception {
//		PostMethod method = new PostMethod(baseUri + "executions/" + executionId + "/details/" + uid + "/file/");
//		Part[] parts = new Part[] { new FilePart(file.getName(), file) };
//		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
//		final int responseCode = client.executeMethod(method);
//		handleResponseCode(method, responseCode);
//	}
//
//	private void handleResponseCode(HttpMethod method, int responseCode) throws Exception {
//		if (responseCode != 200 && responseCode != 204) {
//			throw new Exception("Request was not successful. Response is: " + responseCode + ".\n Response body: "
//					+ method.getResponseBodyAsString());
//		}
//
//	}
//
//}
