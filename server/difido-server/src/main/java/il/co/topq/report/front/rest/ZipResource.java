package il.co.topq.report.front.rest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

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
import org.zeroturnaround.zip.ZipUtil;

import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataProvider;

@RestController
@Path("api/zip")
public class ZipResource {

	private static final Logger log = LoggerFactory.getLogger(ZipResource.class);

	private final MetadataProvider metadataProvider;

	@Autowired
	public ZipResource(MetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider;
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{execution: [0-9]+}")
	public Response get(@PathParam("execution") int executionId) {
		final java.nio.file.Path destinationPath = archiveExecutionReports(executionId);
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

	private java.nio.file.Path archiveExecutionReports(int executionId) {
		final ExecutionMetadata metadata = metadataProvider.getMetadata(executionId);
		if (null == metadata) {
			log.error("No execution with id " + executionId + " was found");
			return null;
		}
		final File source = getExecutionFolder(metadata);
		if (!source.exists()) {
			log.error("Report folder of execution " + executionId + " was not found");
			return null;
		}
		File destination = null;
		try {
			destination = File.createTempFile("execution" + executionId + "_", ".zip");
		} catch (IOException e) {
			log.error("Failed to create temp file",e);
			return null;
		}
		ZipUtil.pack(source, destination);
		log.debug("Temporary file with report of execution " + executionId + " was created in "
				+ destination.getAbsolutePath());
		final java.nio.file.Path destinationPath = destination.toPath();
		return destinationPath;
	}

	private static File getExecutionFolder(ExecutionMetadata metadata) {
		return new File(Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER) + File.separator
				+ Common.REPORTS_FOLDER_NAME + File.separator + metadata.getFolderName());
	}
}
