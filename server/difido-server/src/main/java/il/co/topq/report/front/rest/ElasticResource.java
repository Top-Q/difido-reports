package il.co.topq.report.front.rest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;

@RestController
@Path("api/elastic")
public class ElasticResource {

	private static final Logger log = LoggerFactory.getLogger(ElasticResource.class);
	
	private static final int DEFAULT_ELASTIC_PORT = 9200;
	
	private RestTemplate template;

	private URL base;

	public ElasticResource() {
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_ELASTIC_SEARCH)){
			log.debug("Elastic is disabled. aborting");
			return;
		}

		String elasticHost = null;
		int elasticPort = 0;
		if (Configuration.INSTANCE.readBoolean(ConfigProps.EXTERNAL_ELASTIC)) {
			elasticHost = Configuration.INSTANCE.readString(ConfigProps.EXTERNAL_ELASTIC_HOST);
			elasticPort = Configuration.INSTANCE.readInt(ConfigProps.EXTERNAL_ELASTIC_PORT);
		} else {
			elasticHost = "localhost";
			elasticPort = DEFAULT_ELASTIC_PORT;
		}
		try {
			base = new URL("http://" + elasticHost +":" + elasticPort +"/");
		} catch (MalformedURLException e) {
			log.error("Failed to create url due to " + e.getMessage());
		}
		
		template = new RestTemplate();

	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public String post(String body) {
		log.debug("PUT - Query from Elastic");
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_ELASTIC_SEARCH)){
			log.debug("Elastic is disabled. aborting");
			return "";
		}
		if (null == base){
			log.debug("Elastic location was not found. aborting");
			return "";
		}
		ResponseEntity<String> response = template.postForEntity(base.toString()+ "report/test/_search?pretty=true",body, String.class);
		return response.getBody();
	}

}
