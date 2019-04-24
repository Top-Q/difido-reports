package il.co.topq.report.front.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.FileAddedToTestEvent;
import il.co.topq.report.persistence.MetadataRepository;

@RestController
public class FileUploadResource {

	private final Logger log = LoggerFactory.getLogger(FileUploadResource.class);

	private final ApplicationEventPublisher publisher;

	private MetadataRepository metadataRepository;

	@Autowired
	public FileUploadResource(ApplicationEventPublisher publisher, MetadataRepository metadataRepository) {
		super();
		this.publisher = publisher;
		this.metadataRepository = metadataRepository;
	}

	@RequestMapping(value = "/api/executions/{execution}/details/{uid}/file", method = RequestMethod.POST)
	public @ResponseBody void handleFileUpload(@Context HttpServletRequest request, @PathVariable("execution") int executionId,
			@PathVariable String uid, @RequestParam("file") MultipartFile file) {
		log.debug("POST (" + request.getRemoteAddr() + ") - Attach file '" + file.getName() + "' to test with uid "
				+ uid + " in execution " + executionId);
		if (!file.isEmpty()) {
			try {
				final ExecutionMetadata metadata = metadataRepository.findById(executionId);
				if (null == metadata) {
					log.warn("Request from " + request.getRemoteAddr() + " to add file to execution " + executionId
							+ "failed since metadata is null");
				}
				publisher.publishEvent(
						new FileAddedToTestEvent(executionId, uid, file.getBytes(), file.getOriginalFilename()));
			} catch (IOException e1) {
				log.warn("Request from " + request.getRemoteAddr() + " to get content of file with name "
						+ file.getOriginalFilename() + " failed", e1);
			}
		}
	}

}