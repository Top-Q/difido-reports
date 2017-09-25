package il.co.topq.report.front.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataProvider;
import il.co.topq.report.events.MachineCreatedEvent;

@RestController
@Path("api/executions/{execution}/machines")
public class MachineResource {

	private final Logger log = LoggerFactory.getLogger(MachineResource.class);

	private final ApplicationEventPublisher publisher;

	private final MetadataProvider metadataProvider;

	@Autowired
	public MachineResource(MetadataProvider metadataProvider, ApplicationEventPublisher publisher) {
		this.metadataProvider = metadataProvider;
		this.publisher = publisher;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int addNewMachine(@PathParam("execution") int executionId, MachineNode machine) {
		log.debug("POST - Add new machine to execution " + executionId);
		if (null == machine) {
			throw new WebApplicationException("Machine can't be null");
		}
		final ExecutionMetadata metadata = metadataProvider.getMetadata(executionId);
		if (null == metadata) {
			throw new WebApplicationException("Execution with id " + executionId + " is not exist");
		}
		metadata.getExecution().addMachine(machine);
		publisher.publishEvent(new MachineCreatedEvent(metadata, machine));
		return metadata.getExecution().getMachines().indexOf(machine);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{machine}")
	public void updateMachine(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			MachineNode machine) {
		log.debug("PUT - Update machine to execution with id " + executionId);
		if (null == machine) {
			log.error("Trying to update machine with null machine");
			throw new WebApplicationException("Machine can't be null");
		}
		final ExecutionMetadata metadata = metadataProvider.getMetadata(executionId);
		if (null == metadata) {
			log.error("Execution with id " + executionId + " is not exist");
			throw new WebApplicationException("Execution with id " + executionId + " is not exist");
		}
		if (null == metadata.getExecution()) {
			log.error("Metadata of execution with id " + executionId + " exists but the execution is null");
			throw new WebApplicationException(
					"Metadata of execution with id " + executionId + " exists but the execution is null");
		}
		if (null == metadata.getExecution().getMachines()) {
			log.error("Trying to update machines in execution " + executionId + "while no machines were added");
			throw new WebApplicationException(
					"Trying to update machines in execution " + executionId + "while no machines were added");
		}
		if (null == metadata.getExecution().getMachines().get(machineId)) {
			log.error("Trying to update none existing machine with id " + machineId + " in execution " + executionId);
			throw new WebApplicationException(
					"Trying to update none existing machine with id " + machineId + " in execution " + executionId);
		}
		metadata.getExecution().getMachines().set(machineId, machine);
		publisher.publishEvent(new MachineCreatedEvent(metadata, machine));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{machine}")
	public MachineNode getSingleMachine(@PathParam("execution") int execution, @PathParam("machine") int machine) {
		log.debug("GET - Get machine from execution with id " + execution + " and machine id " + machine);
		return metadataProvider.getMetadata(execution).getExecution().getMachines().get(machine);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<MachineNode> getMachines(@PathParam("execution") int execution) {
		log.debug("GET - Get machines from execution with id " + execution);
		return metadataProvider.getMetadata(execution).getExecution().getMachines();
	}

}
