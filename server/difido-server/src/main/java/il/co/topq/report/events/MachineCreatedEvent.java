package il.co.topq.report.events;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.report.business.execution.ExecutionMetadata;

public class MachineCreatedEvent extends AbsMetadataEvent {

	private final MachineNode machineNode;

	public MachineCreatedEvent(ExecutionMetadata executionMetadata, MachineNode machineNode) {
		super(executionMetadata);
		this.machineNode = machineNode;
	}

	public MachineNode getMachineNode() {
		return machineNode;
	}

}
