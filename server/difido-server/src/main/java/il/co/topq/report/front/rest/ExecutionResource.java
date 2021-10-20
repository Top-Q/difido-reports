package il.co.topq.report.front.rest;

import static il.co.topq.report.StopWatch.newStopWatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.StopWatch;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionCreatedEvent;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.ExecutionUpdatedEvent;
import il.co.topq.report.persistence.ExecutionRepository;
import il.co.topq.report.persistence.ExecutionState;
import il.co.topq.report.persistence.ExecutionStateRepository;
import il.co.topq.report.persistence.MetadataRepository;

@RestController
@Path("api/executions")
public class ExecutionResource {

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	private final ApplicationEventPublisher publisher;

	private MetadataRepository metadataRepository;

	private ExecutionRepository executionRepository;

	private ExecutionStateRepository stateRepository;

	@Autowired
	public ExecutionResource(ApplicationEventPublisher publisher, ExecutionRepository executionRepository,
			MetadataRepository metadataRepository, ExecutionStateRepository stateRepository) {
		this.publisher = publisher;
		this.executionRepository = executionRepository;
		this.metadataRepository = metadataRepository;
		this.stateRepository = stateRepository;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution: [0-9]+}")
	public ExecutionMetadata getMetadata(@Context HttpServletRequest request, @PathParam("execution") int executionId) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Get metadata of execution with id " + executionId);
		return metadataRepository.findById(executionId);
	}

	/**
	 * Get the state of single execution.
	 * 
	 * @param request
	 * @param executionId
	 * @return Representation of the execution state
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution: [0-9]+}/state")
	public ExecutionState getExecutionState(@Context HttpServletRequest request,
			@PathParam("execution") int executionId) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Get Execution state of execution with id " + executionId);
		ExecutionState state = stateRepository.findByMetadataId(executionId);
		if (null == state) {
			log.warn("Tried to get state of execution with id " + executionId + " which are not exists");
			return null;
		}
		// We can't return the state as is since it includes the metadata so we
		// set the metadata to null.
		// This is OK since we are not saving the state to the persistence so it
		// won't affect anything.
		state.setMetadata(null);
		return state;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExecutionMetadata> getMetadata(@Context HttpServletRequest request, @QueryParam("from") String from,
			@QueryParam("to") String to) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Get all metadata ");
		try {
			if (from != null && to != null) {
				final Date dateFrom = DATE_FORMAT.parse(from);
				final Date dateTo = DATE_FORMAT.parse(to);
				return metadataRepository.findAllByTimestampBetween(dateFrom, dateTo);
			} else if (from != null && to == null) {
				final Date dateFrom = DATE_FORMAT.parse(from);
				return metadataRepository.findAllWithTimestampAfter(dateFrom);
			} else if (from == null && to != null) {
				final Date dateTo = DATE_FORMAT.parse(to);
				return metadataRepository.findAllWithTimestampBefore(dateTo);
			}

		} catch (ParseException e) {
			log.error("Failed to parse date " + from + " or " + to, e);
			return null;
		}
		return metadataRepository.findAll();
	}

	/**
	 * Sets the execution properties in the execution meta data. Will allow addition
	 * only of properties that are specified in the configuration file
	 * 
	 * @param metaData
	 * @param executionDetails
	 */
	private void setAllowedPropertiesToMetaData(ExecutionMetadata metaData, ExecutionDetails executionDetails) {
		final List<String> allowedProperties = Configuration.INSTANCE.readList(ConfigProps.CUSTOM_EXECUTION_PROPERTIES);
		if (allowedProperties.isEmpty()) {
			metaData.setProperties(executionDetails.getExecutionProperties());
			return;
		}
		for (String executionProp : executionDetails.getExecutionProperties().keySet()) {
			if (allowedProperties.contains(executionProp)) {
				metaData.addProperty(executionProp, executionDetails.getExecutionProperties().get(executionProp));
			}
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(@Context HttpServletRequest request, ExecutionDetails executionDetails) {
		log.debug("POST (" + request.getRemoteAddr() + ") - Adding new execution ");
		ExecutionMetadata metadata = null;
		ExecutionState state = null;
		if (executionDetails.isShared() && !executionDetails.isForceNew()) {
			metadata = getSharedMatadata();
			if (null == metadata) {
				log.debug("POST (" + request.getRemoteAddr()
						+ ") - Could not find an active shared execution. Creating a new execution");
			} else {
				state = getState(metadata.getId());
			}
		}
		if (metadata == null) {
			metadata = createMetadata(executionDetails);
		}
		if (state == null) {
			state = createState(metadata);
		}
		publisher.publishEvent(new ExecutionCreatedEvent(metadata));
		return state.getId();

	}

	private ExecutionState getState(int id) {
		return stateRepository.findOne(id);
	}

	private ExecutionMetadata getSharedMatadata() {
		List<ExecutionState> activeStates = stateRepository.findByActive(true);
		for (ExecutionState activeState : activeStates) {
			ExecutionMetadata metadata = metadataRepository.findOne(activeState.getId());
			if (metadata.isShared()) {
				return metadata;
			}
		}
		return null;
	}

	private ExecutionMetadata createMetadata(ExecutionDetails executionDetails) {
		StopWatch stopWatch = newStopWatch(log).start("Creating new metadata");
		Execution execution = new Execution();
		final Date executionDate = new Date();
		final ExecutionMetadata metaData = new ExecutionMetadata(executionDate);
		// We want to generate the id
		metadataRepository.save(metaData);
		metaData.setFolderName(Common.EXECUTION_REPORT_FOLDER_PREFIX + "_" + metaData.getId());
		metaData.setUri(Common.REPORTS_FOLDER_NAME + "/" + metaData.getFolderName() + "/index.html");
		metaData.setComment("");
		if (executionDetails != null) {
			metaData.setDescription(executionDetails.getDescription());
			metaData.setSerialNum(""); // empty string to serial column before update
			metaData.setShared(executionDetails.isShared());
			setAllowedPropertiesToMetaData(metaData, executionDetails);
		}
		metadataRepository.save(metaData);
		executionRepository.save(metaData.getId(), execution);
		stopWatch.stopAndLog();
		return metaData;
	}

	private ExecutionState createState(ExecutionMetadata metadata) {
		ExecutionState state = new ExecutionState();
		state.setMetadata(metadata);
		state.setActive(true);
		state.setLocked(false);
		state.setHtmlExists(true);
		state.setId(metadata.getId());
		stateRepository.save(state);
		return state;
	}

	/**
	 * Used to update that a single execution should not be active any more. This is
	 * Irreversible. Also allows updating the execution description & comment
	 * through the metadata parameter
	 * 
	 * @param executionId - the id of the execution
	 * @param active      - Set to not active
	 * @param locked      - Set the execution to locked. Will no be deleted
	 * @param metadataStr - String of key-value pairs to allow updating execution
	 *                    description, comment and possibly other parameters in the
	 *                    future,
	 */
	@PUT
	@Path("/{execution: [0-9]+}")
	public void put(@Context HttpServletRequest request, @PathParam("execution") int executionId,
			@QueryParam("active") Boolean active, @QueryParam("locked") Boolean locked, @QueryParam("serial") String serialNum, 
			@QueryParam("metadata") String metadataStr) {

		log.debug("PUT (" + request.getRemoteAddr() + ") - Upating execution with id " + executionId + ". to active: "
				+ active + ", locked: " + locked + ", serial number: " + serialNum + ", metadata: " + metadataStr);

		final ExecutionState state = stateRepository.findOne(executionId);
		if (null == state) {
			log.warn("Request from " + request.getRemoteAddr() + " to update the state of execution with id "
					+ executionId + " which is not exist");
			return;
		}

		if (active != null && !active) {
			state.setActive(false);
			stateRepository.save(state);
			// TODO: This should be changed to use the executionUpdatedEvent for
			// consistency
			publisher.publishEvent(new ExecutionEndedEvent(executionId));
		}

		if (locked != null) {
			state.setLocked(locked);
			stateRepository.save(state);
			publisher.publishEvent(new ExecutionUpdatedEvent(executionId));
		}

		if (metadataStr != null) {

			String[] keyValuePairs = metadataStr.split("\\\\;");
			for (String keyValuePair : keyValuePairs) {
				String[] keyValueSplit = keyValuePair.split("\\\\=");

				if (keyValueSplit[0].equalsIgnoreCase("description")) {
					if (keyValueSplit.length > 1 && !keyValueSplit[1].trim().equals("")) {
						state.getMetadata().setDescription(keyValueSplit[1]);
					} else {
						state.getMetadata().setDescription("");
					}
				} else if (keyValueSplit[0].equalsIgnoreCase("comment")) {
					if (keyValueSplit.length > 1 && !keyValueSplit[1].trim().equals("")) {
						state.getMetadata().setComment(keyValueSplit[1]);
					} else {
						state.getMetadata().setComment("");
					}
				}
			}
			metadataRepository.save(state.getMetadata());
			publisher.publishEvent(new ExecutionUpdatedEvent(executionId));
		}
		
		if (serialNum != null) {
			state.getMetadata().setSerialNum(serialNum);
			metadataRepository.save(state.getMetadata());
			publisher.publishEvent(new ExecutionUpdatedEvent(executionId));
		}
	}

	/**
	 * Delete a single execution from the server. A notification will be sent to
	 * delete it from all the listeners.
	 * 
	 * @param executionId
	 */
	@DELETE
	@Path("/{execution: [0-9]+}")
	public void delete(@Context HttpServletRequest request, @PathParam("execution") int executionId,
			@DefaultValue("true") @QueryParam("fromElastic") boolean deleteFromElastic) {
		log.debug("DELETE  (" + request.getRemoteAddr() + ") - Delete execution with id " + executionId
				+ ". Delete from Elastic=" + deleteFromElastic);
		final ExecutionState state = stateRepository.findOne(executionId);
		if (null == state) {
			log.warn("Request from " + request.getRemoteAddr() + " to delete execution with index " + executionId
					+ " which is not exist");
			return;
		}
		if (state.isActive()) {
			log.warn("Request from " + request.getRemoteAddr() + " to delete execution with index " + executionId
					+ " which is still active");
			return;
		}
		if (state.isLocked()) {
			log.warn("Request from " + request.getRemoteAddr() + " to delete execution with index " + executionId
					+ " which is locked");
			return;
		}

		publisher.publishEvent(new ExecutionDeletedEvent(executionId, deleteFromElastic));
		executionRepository.delete(executionId);
		stateRepository.delete(state);
		metadataRepository.delete(state.getMetadata());
	}

}
