package il.co.topq.report.front.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.business.execution.ExecutionManager;
import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;
import il.co.topq.report.front.events.ExecutionCreatedEvent;
import il.co.topq.report.front.events.ExecutionDeletedEvent;
import il.co.topq.report.front.events.ExecutionEndedEvent;

@RestController
@Path("api/executions")
public class ExecutionResource {

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	private final ApplicationEventPublisher publisher;

	private final ExecutionManager executionManager;

	@Autowired
	public ExecutionResource(ApplicationEventPublisher publisher, ExecutionManager executionManager) {
		this.publisher = publisher;
		this.executionManager = executionManager;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(ExecutionDetails executionDetails) {
		ExecutionMetadata metadata = null;
		if (null == executionDetails) {
			log.debug("POST - Adding new execution - No description was specified");
			metadata = executionManager.addExecution();

		} else {
			log.debug("POST - Adding new execution " + executionDetails);
			if (executionDetails.isShared() && !executionDetails.isForceNew()) {
				metadata = executionManager.getSharedExecutionIndexAndAddIfNoneExist(executionDetails);
			} else {
				metadata = executionManager.addExecution(executionDetails);
			}
		}
		publisher.publishEvent(new ExecutionCreatedEvent(metadata.getId(), metadata));
		return metadata.getId();

	}

	/**
	 * Used to update that a single execution should not be active any more.
	 * This is Irreversible.
	 * 
	 * @param executionIndex
	 *            the id of the execution
	 * @param active
	 *            - Should the execution set to not active
	 */
	@PUT
	@Path("/{execution: [0-9]+}")
	public void put(@PathParam("execution") int executionIndex, @QueryParam("active") boolean active) {
		log.debug("PUT - Upating execution with id " + executionIndex + ". to active: " + active);
		if (!active) {
			publisher.publishEvent(
					new ExecutionEndedEvent(executionIndex, executionManager.getExecutionMetadata(executionIndex)));
		}
	}

	/**
	 * Delete a single execution from the server. A notification will be sent to
	 * delete it from all the listeners.
	 * 
	 * @param executionIndex
	 */
	@DELETE
	@Path("/{execution: [0-9]+}")
	public void delete(@PathParam("execution") int executionIndex) {
		log.debug("DELETE - Delete execution with id " + executionIndex);
		final ExecutionMetadata executionMetaData = executionManager.getExecutionMetadata(executionIndex);
		if (null == executionMetaData) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is not exist");
			return;
		}
		if (executionMetaData.isActive()) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is still active");
			return;
		}
		publisher.publishEvent(new ExecutionDeletedEvent(executionIndex, executionMetaData));
	}

}
