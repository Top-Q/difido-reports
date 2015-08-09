package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.ExecutionManager;

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

@Path("/executions/{execution}/machines")
public class MachineResource {

	private final Logger log = LoggerFactory.getLogger(MachineResource.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int addNewMachine(@PathParam("execution") int executionId, MachineNode machine) {
		log.debug("POST - Add new machine to execution " + executionId);
		if (null == machine) {
			throw new WebApplicationException("Machine can't be null");
		}
		final Execution execution = ExecutionManager.INSTANCE.getExecution(executionId);
		if (null == execution) {
			throw new WebApplicationException("Execution with id " + executionId + " is not exist");
		}

		if (Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_MERGE_MACHINES)
				&& ExecutionManager.INSTANCE.getExecution(executionId).getMachines() != null
				&& ExecutionManager.INSTANCE.getExecution(executionId).getMachines().contains(machine)) {
			int machineId = ExecutionManager.INSTANCE.getExecution(executionId).getMachines().indexOf(machine);
			MachineNode existMachine = ExecutionManager.INSTANCE.getExecution(executionId).getMachines().get(machineId);
			if (machine.getChildren() != null) {
				for (ScenarioNode scenario : machine.getChildren()) {
					existMachine.addChild(scenario);
				}
			}
			updateMachine(executionId, machineId, existMachine);
			return machineId;
		}
		execution.addMachine(machine);
		ListenersManager.INSTANCE.notifyMachineAdded(executionId, machine);
		return ExecutionManager.INSTANCE.getExecution(executionId).getMachines().indexOf(machine);
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
		final Execution execution = ExecutionManager.INSTANCE.getExecution(executionId);
		if (null == execution) {
			throw new WebApplicationException("Execution with id " + executionId + " is not exist");
		}

		if (Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_MERGE_MACHINES)) {
			if (ExecutionManager.INSTANCE.getExecution(executionId).getMachines() != null
					&& ExecutionManager.INSTANCE.getExecution(executionId).getMachines().contains(machine)) {
				MachineNode existMachine = ExecutionManager.INSTANCE.getExecution(executionId).getMachines()
						.get(machineId);
				if (machine.getChildren() != null) {
					// Scenario
					for (ScenarioNode newScenario : machine.getChildren()) {		
						if (existMachine.getChildren() != null && existMachine.getChildren().contains(newScenario)) {
							int scenarioId = existMachine.getChildren().indexOf(newScenario);
							ScenarioNode oldScenario = existMachine.getChildren().get(scenarioId);
							if (newScenario.getChildren() != null) {
								// Tests
								for (Node newTest : newScenario.getChildren()) {
									if (oldScenario.getChildren() == null || !oldScenario.getChildren().contains(newTest))
										oldScenario.addChild(newTest);
									else
										oldScenario.getChildren().set(oldScenario.getChildren().indexOf(newTest), newTest);
								}
							}
						} else {
							existMachine.addChild(newScenario);
						}
					}
				}
				execution.getMachines().set(machineId, existMachine);
				ListenersManager.INSTANCE.notifyMachineAdded(executionId, existMachine);
			}
			return;
		}

		execution.getMachines().set(machineId, machine);
		ListenersManager.INSTANCE.notifyMachineAdded(executionId, machine);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{machine}")
	public MachineNode get(@PathParam("execution") int execution, @PathParam("machine") int machine) {
		log.debug("GET - Get machine from execution with id " + execution + " and machine id " + machine);
		return ExecutionManager.INSTANCE.getExecution(execution).getMachines().get(machine);
	}
}
