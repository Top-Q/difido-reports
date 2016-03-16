package il.co.topq.report.front.rest;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;

public class DifidoClient {

	private URL base;

	private RestTemplate template;

	public DifidoClient(URL base) {
		this.base = base;
		template = new RestTemplate();
	}

	public int addExecution(ExecutionDetails details) throws Exception {
		ResponseEntity<String> response = template.postForEntity(base.toString() + "/executions", details,
				String.class);
		return Integer.parseInt(response.getBody());
	}

	public void endExecution(int executionId) throws Exception {
		template.put(base + "/executions/" + executionId + "?active=false", executionId);
	}

	public int addMachine(int executionId, MachineNode machine) throws Exception {
		ResponseEntity<String> response = template
				.postForEntity(base.toString() + "executions/" + executionId + "/machines/", machine, String.class);
		return Integer.parseInt(response.getBody());
	}

	public void updateMachine(int executionId, int machineId, MachineNode machine) throws Exception {
		template.put(base.toString() + "executions/" + executionId + "/machines/" + machineId, machine);
	}

	public void addTestDetails(int executionId, TestDetails testDetails) throws Exception {
		template.postForEntity(base.toString() + "executions/" + executionId + "/details", testDetails, Void.class);
	}

	public void addFile(int executionId, String uid, File file) throws Exception {
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file", new ClassPathResource(file.getName()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
				map, headers);
		template.exchange(base.toString() + "executions/" + executionId + "/details/" + uid + "/file/", HttpMethod.POST,
				requestEntity, Void.class);
				// MultiValueMap<String, Object> mvm = new
				// LinkedMultiValueMap<String, Object>();
				// mvm.add("bin", file); // MultipartFile
				//
				// template.postForObject(base.toString() + "executions/" +
				// executionId + "/details/" + uid + "/file/", mvm,
				// Map.class);

		// final HttpPost request = new HttpPost(baseUri + "executions/" +
		// executionId + "/details/" + uid + "/file/");
		// HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("bin",
		// new
		// FileBody(file)).build();
		// request.setEntity(reqEntity);
		// final HttpResponse response = client.execute(request);
		// handleResponse(response);
	}

}
