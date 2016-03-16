package il.co.topq.report.front.events;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

public class MachineCreatedEvent extends AbsMetadataEvent{
	
	private final int executionId;
	
	private final MachineNode machineNode;
	
	public MachineCreatedEvent(int executionId, ExecutionMetadata executionMetadata, MachineNode machineNode) {
		super(executionMetadata);
		this.executionId = executionId;
		this.machineNode = machineNode;
	}

	public int getExecutionId() {
		return executionId;
	}

	public MachineNode getMachineNode() {
		return machineNode;
	}



	
	
	
	
	
}
