package il.co.topq.report.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.model.Session;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/executions")
public class ExecutionResource {

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String post(){
		return Integer.toString(Session.INSTANCE.addExecution());
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Execution get(){
		return Session.INSTANCE.getExecution();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution}")
	public Execution get(@PathParam("execution") int execution){
		return Session.INSTANCE.getExecution(execution);
	}
	
}
