package il.co.topq.report.front.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.Configuration;

@RestController
@Path("api/settings")
public class SettingsResource {
	
	private static final Logger log = LoggerFactory.getLogger(SettingsResource.class);
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get(){
		log.debug("GET - Get the settings");
		return Configuration.INSTANCE.toString();
	}
	
}
