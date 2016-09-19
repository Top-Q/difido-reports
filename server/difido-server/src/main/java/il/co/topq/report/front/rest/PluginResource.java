package il.co.topq.report.front.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
	 * Trigger a specific plugin
	 * 
	 * curl -v "http://localhost:8080/api/plugins/defaultMailPlugin?params=FOO&executions=3&executions=7"
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
	public void get(@PathParam("plugin") String plugin, @QueryParam("executions") List<Integer> executions, @QueryParam("params") String params) {
		log.debug("GET - Execute plugin " + plugin + "(" + params + ") on " + executions.size() +" execution(s)");
		pluginController.executePlugin(plugin,executions, params);
	}

	/**
	 * curl http://localhost:8080/api/plugins
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getPluginNames() {
		log.debug("GET - Getting the list of all plugins");
		return pluginController.getPluginNames();
	}

}
