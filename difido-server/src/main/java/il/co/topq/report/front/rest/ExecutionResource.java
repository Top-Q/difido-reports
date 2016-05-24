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
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataCreator;
import il.co.topq.report.business.execution.MetadataProvider;
import il.co.topq.report.events.ExecutionCreatedEvent;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.ExecutionUpdatedEvent;

@RestController
@Path("api/executions")
public class ExecutionResource {

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	private final ApplicationEventPublisher publisher;

	private final MetadataProvider metadataProvider;

	private final MetadataCreator metadataCreator;
	
	@Autowired
	public ExecutionResource(ApplicationEventPublisher publisher, MetadataProvider metadataProvider,
			MetadataCreator metadataCreator) {
		this.publisher = publisher;
		this.metadataCreator = metadataCreator;
		this.metadataProvider = metadataProvider;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(ExecutionDetails executionDetails) {
		ExecutionMetadata metadata = null;
		log.debug("POST - Adding new execution");
		if (executionDetails != null && executionDetails.isShared() && !executionDetails.isForceNew()) {
			metadata = metadataProvider.getShared();
			if (null == metadata) {
				log.debug("POST - Could not find an active shared execution. Creating a new execution");
				metadata = metadataCreator.createMetadata(executionDetails);
			}
		} else {
			metadata = metadataCreator.createMetadata(executionDetails);
		}
		publisher.publishEvent(new ExecutionCreatedEvent(metadata));
		return metadata.getId();

	}

	/**
	 * Used to update that a single execution should not be active any more.
	 * This is Irreversible.
	 * 
	 * @param executionIndex
	 *            the id of the execution
	 * @param active
	 *            - Set to not active
	 * @param locked
	 *            - Set the execution to locked. Will no be deleted
	 * 
	 */
	@PUT
	@Path("/{execution: [0-9]+}")
	public void put(@PathParam("execution") int executionIndex, @QueryParam("active") Boolean active,
			@QueryParam("locked") Boolean locked) {
		log.debug("PUT - Upating execution with id " + executionIndex + ". to active: " + active + " and locked: "
				+ locked);
		final ExecutionMetadata metadata = metadataProvider.getMetadata(executionIndex);

		if (active != null && !active) {
			// TODO: This should be changed to use the executionUpdatedEvent for
			// consistency
			publisher.publishEvent(new ExecutionEndedEvent(metadata));
		}
		if (locked != null) {
			metadata.setLocked(locked);
			publisher.publishEvent(new ExecutionUpdatedEvent(metadata));
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
		final ExecutionMetadata executionMetaData = metadataProvider.getMetadata(executionIndex);
		if (null == executionMetaData) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is not exist");
			return;
		}
		if (executionMetaData.isActive()) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is still active");
			return;
		}
		if (executionMetaData.isLocked()) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is locked");
			return;
		}
		publisher.publishEvent(new ExecutionDeletedEvent(executionIndex, executionMetaData));
	}

}
