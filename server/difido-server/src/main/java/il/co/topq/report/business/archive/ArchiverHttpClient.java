package il.co.topq.report.business.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides simple HTTP operations.
 * 
 *
 */
public class ArchiverHttpClient {

	private final Logger log = LoggerFactory.getLogger(ArchiverHttpClient.class);

	private final RestTemplate restTemplate;

	private final String host;

	public ArchiverHttpClient(String host) {
		this.host = host;
		this.restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

	}

	@SuppressWarnings("unchecked")
	public <T> T get(String path, @SuppressWarnings("rawtypes") TypeReference valueTypeRef) {
		ResponseEntity<String> response = restTemplate.getForEntity(host + path, String.class);
		try {
			return (T) new ObjectMapper().readValue(response.getBody(), valueTypeRef);
		} catch (Exception e) {
			log.error("Failed to read value from " + host + path, e);
		}
		return null;
	}

	/**
	 * Get file from remote machine using HTTP
	 * 
	 * @param path
	 * 		Folder path
	 * @param fileName
	 * 		Filename
	 * @return
	 * 		File object
	 */
	public File getFile(String path, String fileName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<byte[]> response = null;

		try {
			response = restTemplate.exchange(host + path, HttpMethod.GET, entity, byte[].class, "1");
		}catch (RestClientException e) {
			log.error("Fail to download file: " +  fileName, e);
			return null;
		}

		final File file = new File(System.getProperty("java.io.tmpdir"), fileName);
		if (file.exists()) {
			file.delete();
		}
		if (response.getStatusCode() == HttpStatus.OK) {
			try {
				Files.write(file.toPath(), response.getBody());
			} catch (IOException e) {
				log.error("Failed to download file from path " + path, e);
				return null;
			}
		}
		return file;
	}

	/**
	 * Calls to HTTP delete command
	 * 
	 * @param path
	 * 		Relative resource path
	 */
	public void delete(String path) {
		try {
			restTemplate.delete(host + path);
		} catch (RestClientException e) {
			log.error("Failed to send delete to " + host + path, e);
		}
	}

	/**
	 * Calls to HTTP GET command and parses the result to String
	 * 
	 * @param path
	 * 		Relative resource path	 *
	 * @return Result as string
	 */
	public String getString(String path) {
		try {
			return restTemplate.getForObject(host + path, String.class);
		} catch (RestClientException e) {
			log.error("Failed to get String response from " + host + path, e);
		}
		return null;

	}

}
