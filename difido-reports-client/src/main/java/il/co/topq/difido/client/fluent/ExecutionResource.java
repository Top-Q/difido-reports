package il.co.topq.difido.client.fluent;

import javax.ws.rs.client.WebTarget;

public class ExecutionResource {

	private final WebTarget baseTarget;

	private final int index;

	public ExecutionResource(WebTarget baseTarget, int index) {
		super();
		this.baseTarget = baseTarget;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public MachineResource addMachine() {
		
		return new MachineResource(baseTarget, index);
	}

}
