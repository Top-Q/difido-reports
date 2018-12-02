package il.co.topq.report.front.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.StopWatch;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataProvider;
import il.co.topq.report.business.report.ExecutionTableService;
import il.co.topq.report.business.report.ArchiveService;

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
	
	@Autowired
	public ArchiveService zipService;
	
	
	/**
	 * Get list of all the reports
	 * 
	 * @param execution
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DataTable get() {
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
	
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{execution: [0-9]+}")
	public Response getReportAsZip(@PathParam("execution") int executionId) {
		final ExecutionMetadata metadata = metadataProvider.getMetadata(executionId);
		if (null == metadata) {
			log.error("No execution with id " + executionId + " was found");
			return null;
		}
		StopWatch stopWatch = new StopWatch(log).start("Archiving HTML reports");
		final java.nio.file.Path destinationPath = zipService.archiveReports(metadata);
		stopWatch.stopAndLog();
		if (null == destinationPath) {
			return null;
		}
		return generateResponse(executionId, destinationPath).build();
	}

	private ResponseBuilder generateResponse(int executionId, final java.nio.file.Path destinationPath) {
		final ResponseBuilder response = Response.ok().entity(new StreamingOutput() {
			@Override
			public void write(final OutputStream output) throws IOException, WebApplicationException {
				try {
					Files.copy(destinationPath, output);
				} finally {
					// We would like to delete the file after sending it
					Files.delete(destinationPath);
				}
			}
		});
		response.header("Content-Disposition", "attachment; filename=\"execution" + executionId + "\"");
		return response;
	}



}