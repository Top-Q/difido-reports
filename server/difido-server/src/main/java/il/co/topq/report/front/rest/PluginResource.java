package il.co.topq.report.front.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.plugins.PluginController;
import il.co.topq.report.persistence.ExecutionRepository;
import il.co.topq.report.persistence.MetadataRepository;

@RestController
@Path("api/plugins")
public class PluginResource {

	private static final Logger log = LoggerFactory.getLogger(PluginResource.class);

	private PluginController pluginController;

	private final MetadataRepository metadataRepository;

	private final ExecutionRepository executionRepository;

	@Autowired
	public PluginResource(PluginController pluginController, MetadataRepository metadataRepository,
			ExecutionRepository executionRepository) {
		this.pluginController = pluginController;
		this.metadataRepository = metadataRepository;
		this.executionRepository = executionRepository;
	}

	/**
	 * Trigger a specific plugin
	 * 
	 *
	 * @param request
	 * 			   The servlet request
	 * @param plugin
	 *            The name of the plugin to execute
	 * @param executions
	 *            The id of the executions to execute the plugin on
	 * @param params
	 *            Free parameter string
	 *
	 * @return The plugin response
	 * 
	 * 
	 */
	@POST
	@Path("{plugin}")
	@Produces(MediaType.TEXT_HTML)
	public String post(@Context HttpServletRequest request, @PathParam("plugin") String plugin,
			@QueryParam("executions") List<Integer> executions, @QueryParam("params") String params) {
		log.debug("POST (" + request.getRemoteAddr() + ") - Execute plugin " + plugin + "(" + params + ") on "
				+ executions.size() + " execution(s)");
		final List<ExecutionMetadata> metaDataList = new ArrayList<ExecutionMetadata>();
		final List<Execution> executionList = new ArrayList<Execution>();
		if (executions != null) {
			for (int executionId : executions) {
				final ExecutionMetadata metaData = metadataRepository.findById(executionId);
				if (metaData != null) {
					metaDataList.add(metaData);

				}
				final Execution execution = executionRepository.findById(executionId);
				executionList.add(execution);
			}
		}
		return pluginController.executeInteractivePlugin(plugin, metaDataList, executionList, params);
	}

	/**
	 * Get the list of all the plugin names
	 * 
	 * @param request
	 * 		HTTP Request
	 *
	 *
	 * @return List of the named of all the currently installed plugins
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getPluginNames(@Context HttpServletRequest request) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Getting the list of all plugins");
		return pluginController.getPluginNames();
	}

}
