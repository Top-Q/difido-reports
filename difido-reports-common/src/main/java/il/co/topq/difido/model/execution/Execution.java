package il.co.topq.difido.model.execution;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Execution {

	private List<MachineNode> machines;

	public List<MachineNode> getMachines() {
		return machines;
	}

	public void setMachines(List<MachineNode> machines) {
		this.machines = machines;
	}

	public synchronized void addMachine(MachineNode machine) {
		// NOTE: Do not remove the the 'synchronized' key word!
		if (machines == null) {
			// We use it to avoid ConcurrentModificationException
			machines = new CopyOnWriteArrayList<MachineNode>();
		}
		machines.add(machine);
	}

	/**
	 * 
	 * @param machineName
	 * @return The first machine with the specified name or null if none exist
	 */
	@JsonIgnore
	public MachineNode getMachineByName(String machineName) {
		if (null == machineName) {
			return null;
		}
		if (null == machines) {
			return null;
		}
		if (machines.isEmpty()) {
			return null;
		}
		for (MachineNode machine : machines) {
			if (machine.getName().trim().equals(machineName.trim())) {
				return machine;
			}
		}
		return null;
	}

	@JsonIgnore
	public MachineNode getLastMachine() {
		if (null == machines) {
			return null;
		}
		return machines.get(machines.size() - 1);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Execution").append("\n");

		if (null != machines) {
			sb.append("Machines: ").append(Arrays.toString(machines.toArray())).append("\n");
		}
		return sb.toString();
	}

}
