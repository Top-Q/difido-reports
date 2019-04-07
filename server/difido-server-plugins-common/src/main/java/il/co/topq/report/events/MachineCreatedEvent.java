package il.co.topq.report.events;

import il.co.topq.difido.model.execution.MachineNode;

public class MachineCreatedEvent extends AbsMetadataEvent {

	private final MachineNode machineNode;

	public MachineCreatedEvent(int executionId, MachineNode machineNode) {
		super(executionId);
		this.machineNode = machineNode;
	}

	public MachineNode getMachineNode() {
		return machineNode;
	}

}
