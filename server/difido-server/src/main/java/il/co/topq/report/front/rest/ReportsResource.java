package il.co.topq.report.front.rest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.StopWatch;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.report.ArchiveService;
import il.co.topq.report.business.report.ExecutionTableService;
import il.co.topq.report.events.UpdateAllMetadatasRequestEvent;
import il.co.topq.report.persistence.ExecutionState;
import il.co.topq.report.persistence.ExecutionStateRepository;
import il.co.topq.report.persistence.MetadataRepository;

@RestController
@Path("api/reports")
public class ReportsResource {

	private static final Logger log = LoggerFactory.getLogger(ReportsResource.class);

	private final MetadataRepository metadataRepository;
	
	private final ApplicationEventPublisher publisher;
	
	private final ExecutionStateRepository stateRepository;

	@Autowired
	public ReportsResource(MetadataRepository metadataRepository, ExecutionStateRepository stateRepository,ApplicationEventPublisher publisher) {
		this.metadataRepository = metadataRepository;
		this.stateRepository = stateRepository;
		this.publisher = publisher;
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
	public DataTable get(@Context HttpServletRequest request) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Get all reports");
		StopWatch stopWatch = new StopWatch(log).start("Getting all metaData");
		publisher.publishEvent(new UpdateAllMetadatasRequestEvent());
		final List<ExecutionMetadata> metadataList = metadataRepository.findAll();
		stopWatch.stopAndLog();

		stopWatch.start("Getting all states");
		final List<ExecutionState> stateList = stateRepository.findAll();
		stopWatch.stopAndLog();
		
		stopWatch.start("Initilaizing table");
		final DataTable dataTable = executionTableService.initTable(metadataList,stateList);
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
	public Response getReportAsZip(@Context HttpServletRequest request, @PathParam("execution") int executionId) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Recieved request for getting execution " + executionId
				+ " as ZIP");
		final ExecutionMetadata metadata = metadataRepository.findById(executionId);
		if (null == metadata) {
			log.error("Request from " + request.getRemoteAddr() + " to get report as ZIP of execution id " + executionId
					+ " failed since metadata is null");
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
		response.header("Content-Disposition", "attachment; filename=\"execution" + executionId + ".zip\"");
		return response;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{execution: [0-9]+}/size")
	public long getReportSize(@Context HttpServletRequest request, @PathParam("execution") int executionId) {
		log.debug("GET (" + request.getRemoteAddr() + ")- Recieved request for getting the size of execution "
				+ executionId + " folder");
		final ExecutionMetadata metadata = metadataRepository.findById(executionId);
		long size = 0;
		if (null == metadata) {
			log.error("Request from " + request.getRemoteAddr() + " to get report size of execution with id "
					+ executionId + " failed since to metadata was found");
			return size;
		}
		StopWatch stopWatch = new StopWatch(log).start("Calculating reports folder size");
		try {
			File executionFolder = zipService.getExecutionFolder(metadata);
			if (null == executionFolder || !executionFolder.exists()) {
				log.error("Error getting execution folder for execution " + executionId + ". Recieved "
						+ executionFolder);
				return size;
			}
			size = FileUtils.sizeOfDirectory(executionFolder);
			if (0 == size) {
				log.error("Request from " + request.getRemoteAddr()
						+ " to calculate folder size failed. Check if folder is restricted");
			}

		} finally {
			stopWatch.stopAndLog();
		}

		return size;
	}

}