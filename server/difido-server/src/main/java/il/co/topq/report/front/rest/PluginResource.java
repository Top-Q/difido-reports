package il.co.topq.report.front.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.business.plugins.PluginController;

@RestController
@Path("api/plugins")
public class PluginResource {

	private static final Logger log = LoggerFactory.getLogger(PluginResource.class);

	private PluginController pluginController;

	@Autowired
	public PluginResource(PluginController pluginController) {
		this.pluginController = pluginController;
	}

	/**
	 * Get list of all the reports
	 * 
	 * curl http://localhost:8080/api/plugins/simplePlugin?params=FOO
	 * 
	 * @param pluginName
	 *            The name of the plugin to execute
	 * @param Free
	 *            parameter string
	 *            
	 *            
	 */
	@GET
	@Path("{plugin}")
	public void get(@PathParam("plugin") String plugin, @QueryParam("params") String params) {
		log.debug("GET - Execute plugin " + plugin + "(" + params + ")");
		pluginController.executePlugin(plugin, params);
	}

}
