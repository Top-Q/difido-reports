package il.co.topq.difido.client.fluent;

import javax.ws.rs.client.WebTarget;

public class MachineResource {

	private final WebTarget baseTarget;

	private final int index;

	public MachineResource(WebTarget baseTarget, int index) {
		this.baseTarget = baseTarget;
		this.index = index;
	}

	public int add(String machineName) {
		return 0;
	}
	
	

}
