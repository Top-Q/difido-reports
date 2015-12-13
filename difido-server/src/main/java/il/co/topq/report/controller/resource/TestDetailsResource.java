package il.co.topq.report.controller.resource;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.listener.ListenersManager;

@Path("/executions/{execution}/details")
public class TestDetailsResource {

	private final Logger log = LoggerFactory.getLogger(TestDetailsResource.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void post(@PathParam("execution") int executionId, TestDetails details) {
		log.debug("POST - Add execution details to execution with id " + executionId);
		if (null == details) {
			log.error("Details can't be null");
			throw new WebApplicationException("Details can't be null");
		}

		ListenersManager.INSTANCE.notifyTestDetailsAdded(executionId, details);
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/{uid}/file")
	public void postFile(@PathParam("execution") int executionId, @PathParam("uid") String uid,
			FormDataMultiPart multiPart) {
		log.debug("POST - Add file to execution with id " + executionId);
		FormDataBodyPart fileBodyPart = multiPart.getFields().values().iterator().next().get(0);

		InputStream fileStream = fileBodyPart.getValueAs(InputStream.class);
		FormDataContentDisposition fileDisposition = fileBodyPart.getFormDataContentDisposition();
		String fileName = fileDisposition.getFileName();

		ListenersManager.INSTANCE.notifyFileAddedToTest(executionId, uid, fileStream, fileName);

	}

}
