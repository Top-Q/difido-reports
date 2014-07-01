package il.co.topq.report.resource;

import il.co.topq.report.model.Session;
import il.co.topq.report.model.execution.ReportedMachine;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/executions/{execution}/machines")
public class MachineResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(@PathParam("execution") int execution, ReportedMachine machine) {
		return Session.INSTANCE.getExecution(execution).addMachine(machine);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{machine}")
	public ReportedMachine get(@PathParam("execution") int execution, @PathParam("machine") int machine) {
		return Session.INSTANCE.getExecution(execution).getMachines().get(machine);
	}
}
