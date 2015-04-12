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
		//NOTE: Do not remove the the 'synchronized' key word!
		if (machines == null) {
			//We use it to avoid ConcurrentModificationException
			machines = new CopyOnWriteArrayList<MachineNode>();
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
		StringBuilder sb = new StringBuilder();
		sb.append("Execution").append("\n");
		
		if  (null != machines){
			sb.append("Machines: ").append(Arrays.toString(machines.toArray())).append("\n");
		}
		return sb.toString();
	}

}
