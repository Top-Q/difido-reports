package il.co.topq.report;

import il.co.topq.report.model.execution.ReportedMachine;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;

public abstract class ResourceTests {

	private HttpServer server;
    private WebTarget baseTarget;
	
	@Before
    public void setUp() throws Exception {
        server = Main.startServer();
        Client client = ClientBuilder.newClient();
        baseTarget = client.target(Main.BASE_URI);
        System.out.println("@Before - Grizzly server started on: " + Main.BASE_URI);
    }
	
	@After
    public void tearDown() throws Exception {
        server.shutdownNow();
        System.out.println("\n@After - Grizzly server shut down");
    }

	/**
	 * invoke: ExecutionResource.post()
	 */
	protected int addExecution() {
    	WebTarget executionsTarget = baseTarget.path("/executions");
    	
    	System.out.println("\nAdding new execution -" +
    			"\nPOST request to: " + executionsTarget.getUri());
    	
    	Response response = executionsTarget.request(MediaType.TEXT_PLAIN).post(null);
    	int executionId = Integer.parseInt(response.readEntity(String.class));
    	
    	System.out.println(">> Received execution ID: " + executionId);
    	return executionId;
	}
	
	/**
	 * invoke: MachineResource.post()
	 */
	protected int addMachine(int executionId, String machineName) {
    	WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines");
    	
    	System.out.println("\nAdding new machine to execution <" + executionId + "> -" +
    			"\nPOST request to: " + machinesTarget.getUri());
    	
    	Response postResponse = machinesTarget.request(MediaType.TEXT_PLAIN).post(Entity.entity(new ReportedMachine(machineName), MediaType.APPLICATION_JSON));
    	int machineId = Integer.parseInt(postResponse.readEntity(String.class));
    	
    	System.out.println(">> Received machine ID: " + machineId);
    	return machineId;
	}
	
	/**
	 * invoke: MachineResource.get()
	 */
	protected ReportedMachine getMachine(int executionId, int machineId) {
    	WebTarget machinesTarget = baseTarget.path("/executions/" + executionId + "/machines/" + machineId);
    	
    	System.out.println("\nGetting machine <" + machineId + "> from execution <" + executionId + "> -" +
    			"\nGET request to: " + machinesTarget.getUri());
    	
    	ReportedMachine machine = machinesTarget.request(MediaType.APPLICATION_JSON).get(ReportedMachine.class);
    	
    	System.out.println(">> Received machine with name: \"" + machine.getName() + "\"");
    	return machine;
	}
}
