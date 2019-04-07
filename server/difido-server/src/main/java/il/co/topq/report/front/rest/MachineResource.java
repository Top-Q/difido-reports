package il.co.topq.report.front.rest;

import static il.co.topq.report.StopWatch.newStopWatch;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.report.StopWatch;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.persistence.ExecutionRepository;
import il.co.topq.report.persistence.MetadataRepository;

@RestController
@Path("api/executions/{execution}/machines")
public class MachineResource {

	private final Logger log = LoggerFactory.getLogger(MachineResource.class);

	private final ApplicationEventPublisher publisher;

	private final ExecutionRepository executionRepository;

	private final MetadataRepository metadataRepository;

	@Autowired
	public MachineResource(ApplicationEventPublisher publisher, ExecutionRepository executionRepository,
			MetadataRepository metadataRepository) {
		this.publisher = publisher;
		this.executionRepository = executionRepository;
		this.metadataRepository = metadataRepository;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int addNewMachine(@Context HttpServletRequest request, @PathParam("execution") int executionId,
			MachineNode machine) {
		log.debug("POST (" + request.getRemoteAddr() + ") - Add new machine to execution " + executionId);
		if (null == machine) {
			throw new WebApplicationException("Machine can't be null");
		}
		final Execution execution = executionRepository.findById(executionId);

		StopWatch stopWatch = newStopWatch(log).start("Adding machine to execution");
		execution.addMachine(machine);
		stopWatch.stopAndLog();

		stopWatch = newStopWatch(log).start("Publishing machine create event");
		publisher.publishEvent(new MachineCreatedEvent(executionId, machine));
		stopWatch.stopAndLog();
		return execution.getMachines().indexOf(machine);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{machine}")
	public void updateMachine(@Context HttpServletRequest request, @PathParam("execution") int executionId,
			@PathParam("machine") int machineId, MachineNode machine) {
		log.debug("PUT (" + request.getRemoteAddr() + ") - Update machine to execution with id " + executionId);
		if (null == machine) {
			log.error("Request from (" + request.getRemoteAddr() + ") to update machine with null machine");
			throw new WebApplicationException("Machine can't be null");
		}
		final Execution execution = executionRepository.findById(executionId);
		if (null == execution) {
			log.error("Request from (" + request.getRemoteAddr() + ") to update machine to execution id " + executionId
					+ " which the metadata exists but the execution is null. "
					+ "This can happen due to use trying to update execution that is already done and closed.");
			throw new WebApplicationException(
					"Metadata of execution with id " + executionId + " exists but the execution is null");
		}
		if (null == execution.getMachines()) {
			log.error("Request from (" + request.getRemoteAddr() + ")  to update machines in execution " + executionId
					+ " while no machines were added");
			throw new WebApplicationException(
					"Trying to update machines in execution " + executionId + "while no machines were added");
		}
		if (null == execution.getMachines().get(machineId)) {
			log.error("Request from (" + request.getRemoteAddr() + ") to update none existing machine with id "
					+ machineId + " in execution " + executionId);
			throw new WebApplicationException(
					"Trying to update none existing machine with id " + machineId + " in execution " + executionId);
		}

		StopWatch stopWatch = newStopWatch(log).start("Updating execution properties as scenario properties");
		addExecutionProsAsScenarioProps(executionId, machine);
		stopWatch.stopAndLog();
		stopWatch = newStopWatch(log).start("Updating machine in execution");
		execution.getMachines().set(machineId, machine);
		stopWatch.stopAndLog();

		stopWatch = newStopWatch(log).start("Publishing machine created event");
		publisher.publishEvent(new MachineCreatedEvent(executionId, machine));
		stopWatch.stopAndLog();
	}

	/**
	 * 
	 * Issue #192. We would like to have all the execution properties as
	 * scenario properties so They will be visible in the HTML report and in the
	 * Elastic. This is currently the responsibility of One of the binders.
	 * 
	 * @param machine
	 *            The machine that is updated
	 * @param metadata
	 *            The metadata with the current execution properties
	 */
	private void addExecutionProsAsScenarioProps(int executionId, MachineNode machine) {
		ExecutionMetadata metadata = metadataRepository.findById(executionId);
		if (null == metadata.getProperties()) {
			return;
		}
		if (null == machine.getChildren() || machine.getChildren().isEmpty()) {
			return;
		}
		for (ScenarioNode scenario : machine.getChildren()) {
			for (String key : metadata.getProperties().keySet()) {
				// If the scenario doesn't have scenario properties (null), we
				// can be sure that we are not overriding
				// any properties.
				if (null == scenario.getScenarioProperties() || null == scenario.getScenarioProperties().get(key)) {
					// In case the scenario properties is null, this call will
					// create a new instance.
					scenario.addScenarioProperty(key, metadata.getProperties().get(key));
				}
			}
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{machine}")
	public MachineNode getSingleMachine(@Context HttpServletRequest request, @PathParam("execution") int execution,
			@PathParam("machine") int machine) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Get machine from execution with id " + execution
				+ " and machine id " + machine);
		return executionRepository.findById(execution).getMachines().get(machine);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<MachineNode> getMachines(@Context HttpServletRequest request, @PathParam("execution") int execution) {
		log.debug("GET (" + request.getRemoteAddr() + ") - Get machines from execution with id " + execution);
		return executionRepository.findById(execution).getMachines();
	}

}
