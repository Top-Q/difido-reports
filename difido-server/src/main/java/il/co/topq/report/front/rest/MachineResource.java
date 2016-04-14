package il.co.topq.report.front.rest;

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
			throw new WebApplicationException("Machine can't be null");
		}
		final ExecutionMetadata metadata = metadataProvider.getMetadata(executionId);
		if (null == metadata) {
			throw new WebApplicationException("Execution with id " + executionId + " is not exist");
		}
		metadata.getExecution().getMachines().set(machineId, machine);
		publisher.publishEvent(new MachineCreatedEvent(metadata, machine));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{machine}")
	public MachineNode get(@PathParam("execution") int execution, @PathParam("machine") int machine) {
		log.debug("GET - Get machine from execution with id " + execution + " and machine id " + machine);
		return metadataProvider.getMetadata(execution).getExecution().getMachines().get(machine);
	}
}
