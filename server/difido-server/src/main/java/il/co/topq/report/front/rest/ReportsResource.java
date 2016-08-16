package il.co.topq.report.front.rest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.business.execution.ExecutionTableService;
import il.co.topq.report.business.execution.MetadataProvider;

@RestController
@Path("api/reports")
public class ReportsResource {



	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	private final MetadataProvider metadataProvider;
	
	@Autowired
	public ReportsResource(MetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider;
	}
	
	@Autowired
	public ExecutionTableService executionTableService;

	/**
	 * Get list of all the reports
	 * 
	 * @param execution
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DataTable get(@PathParam("execution") int execution) {
		log.debug("GET - Get all reports");
		return executionTableService.initTable(metadataProvider.getAllMetaData());
	}

	public static class DataTable {
		// Holds the headers of the table. The data structure has to be ordered
		// and not to allow duplications.
		final public Set<String> headers = new LinkedHashSet<String>();
		final public List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	}

}