package il.co.topq.difido.client.fluent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import il.co.topq.difido.model.execution.Execution;

public class DifidoClient {
	
	private final WebTarget baseTarget;
	
	private DifidoClient(String baseUri){
		Client client = ClientBuilder.newClient();
		this.baseTarget = client.target(baseUri);
	}
	
	public static DifidoClient build(String baseUri){
		return new DifidoClient(baseUri);
	}
	
	public ExecutionResource addExecution(){
		WebTarget executionsTarget = baseTarget.path("/executions");
		Response response = executionsTarget.request(MediaType.TEXT_PLAIN).post(null);
		int executionId = Integer.parseInt(response.readEntity(String.class));
		return new ExecutionResource(baseTarget,executionId);
	}
	
	public Execution getExecution(int executionIndex){
		return null;
	}
	
	public ExecutionResource execution(int executionIndex){
		return new ExecutionResource(baseTarget,executionIndex);
	}
	
	public static void main(String[] args) {
		DifidoClient client = DifidoClient.build("");
//		client.addExecution().addMachine().
//		client.execution(3).machine(3).scenario(4).add("name");
//		client.execution(3).machine.add("some name");
//		client.execution(4).machine(4).scenario(4).test.add("sds");
	}
	
	
	
}
