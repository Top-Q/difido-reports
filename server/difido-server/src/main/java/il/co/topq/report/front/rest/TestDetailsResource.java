package il.co.topq.report.front.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.hibernate.annotations.common.reflection.MetadataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.TestDetailsCreatedEvent;

@RestController
@Path("api/executions/{execution}/details")
public class TestDetailsResource {

	private final Logger log = LoggerFactory.getLogger(TestDetailsResource.class);

	private final ApplicationEventPublisher publisher;

	@Autowired
	public TestDetailsResource(ApplicationEventPublisher publisher) {
		super();
		this.publisher = publisher;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void post(@Context HttpServletRequest request, @PathParam("execution") int executionId,
			TestDetails details) {
		log.debug("POST (" + request.getRemoteAddr() + ") - Add execution details to execution with id " + executionId);
		if (null == details) {
			log.error("Request from " + request.getRemoteAddr() + " to update null details");
			throw new WebApplicationException("Details can't be null");
		}
		publisher.publishEvent(new TestDetailsCreatedEvent(executionId, details));
	}

}
