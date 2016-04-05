package il.co.topq.report.front.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.MetadataController;
import il.co.topq.report.business.execution.MetadataController.ExecutionMetadata;
import il.co.topq.report.events.TestDetailsCreatedEvent;

@RestController
@Path("api/executions/{execution}/details")
public class TestDetailsResource {

	private final Logger log = LoggerFactory.getLogger(TestDetailsResource.class);
	
	private final ApplicationEventPublisher publisher;
	
	private final MetadataController executionManager;

	@Autowired
	public TestDetailsResource(ApplicationEventPublisher publisher,MetadataController executionManager) {
		super();
		this.publisher = publisher;
		this.executionManager = executionManager;
	}



	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void post(@PathParam("execution") int executionId, TestDetails details) {
		log.debug("POST - Add execution details to execution with id " + executionId);
		if (null == details) {
			log.error("Details can't be null");
			throw new WebApplicationException("Details can't be null");
		}
		ExecutionMetadata metadata = executionManager.getExecutionMetadata(executionId);
		publisher.publishEvent(new TestDetailsCreatedEvent(executionId, metadata, details));
	}

//	@POST
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.TEXT_PLAIN)
//	@Path("/{uid}/file")
//	public String postFile(@PathParam("execution") int executionId, @PathParam("uid") String uid,
//			FormDataMultiPart multiPart) {
//		log.debug("POST - Add file to execution with id " + executionId);
//		FormDataBodyPart fileBodyPart = multiPart.getFields().values().iterator().next().get(0);
//
//		InputStream fileStream = fileBodyPart.getValueAs(InputStream.class);
//		FormDataContentDisposition fileDisposition = fileBodyPart.getFormDataContentDisposition();
//		String fileName = fileDisposition.getFileName();
//
//		ListenersManager.INSTANCE.notifyFileAddedToTest(executionId, uid, fileStream, fileName);
//		
//		return "Success";
//	}

}
