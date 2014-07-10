package il.co.topq.difido.model.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Execution {

	private List<MachineNode> machines;

	public List<MachineNode> getMachines() {
		return machines;
	}

	public void setMachines(List<MachineNode> machines) {
		this.machines = machines;
	}

	public void addMachine(MachineNode machine) {
		if (machines == null) {
			machines = new ArrayList<MachineNode>();
		}
		machines.add(machine);
	}

	@JsonIgnore
	public MachineNode getLastMachine() {
		if (null == machines) {
			return null;
		}
		return machines.get(machines.size() - 1);

	}
	
	@Override
	public String toString(){
		return Arrays.toString(machines.toArray());
	}

}
