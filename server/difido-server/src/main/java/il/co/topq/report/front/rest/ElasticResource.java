package il.co.topq.report.front.rest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;

@RestController
public class ElasticResource {

	private static final Logger log = LoggerFactory.getLogger(ElasticResource.class);

	private RestTemplate template;

	private URL base;

	private boolean enabled = true;

	public ElasticResource() {
		String elasticHost = null;
		int elasticPort = 0;
		elasticHost = Configuration.INSTANCE.readList(ConfigProps.ELASTIC_HOST).get(0);
		elasticPort = Integer.parseInt(Configuration.INSTANCE.readList(ConfigProps.ELASTIC_HTTP_PORT).get(0));
		try {
			base = new URL("http://" + elasticHost + ":" + elasticPort + "/");
		} catch (MalformedURLException e) {
			log.error("Failed to create url due to " + e.getMessage());
			enabled = false;
			return;
		}
		template = new RestTemplate();
	}

	@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, value = "api/elastic/{index}/{doc}", method = RequestMethod.POST)
	public String post(@DefaultValue("report") @PathVariable("index") String index,
			@DefaultValue("test") @PathVariable("doc") String doc, @RequestBody String body) {
		log.debug("PUT - Query from Elastic");
		if (!enabled) {
			log.debug("Elastic is disabled. aborting");
			return "";
		}
		ResponseEntity<String> response = null;
		try {
			response = template.postForEntity(base.toString() + index + "/" + doc + "/_search?pretty=true", body,
					String.class);

		} catch (RestClientException e) {
			log.warn("Failed to connect to Kibana");
			return "";
		}

		return response.getBody();
	}

}
