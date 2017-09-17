package il.co.topq.report.front.rest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

import il.co.topq.report.StopWatch;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataProvider;
import il.co.topq.report.business.report.ExecutionTableService;

@RestController
@Path("api/reports")
public class ReportsResource {

	private static final Logger log = LoggerFactory.getLogger(ReportsResource.class);

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
		StopWatch stopWatch = new StopWatch(log).start("Getting all metaData");
		final ExecutionMetadata[] metaDataArr = metadataProvider.getAllMetaData();
		stopWatch.stopAndLog();
		
		stopWatch.start("Initilaizing table");
		final DataTable dataTable = executionTableService.initTable(metaDataArr);
		stopWatch.stopAndLog();
		return dataTable;
	}

	public static class DataTable {
		// Holds the headers of the table. The data structure has to be ordered
		// and not to allow duplications.
		final public Set<String> columns = new LinkedHashSet<>();
		final public List<List<String>> data = new ArrayList<>();
	}

}