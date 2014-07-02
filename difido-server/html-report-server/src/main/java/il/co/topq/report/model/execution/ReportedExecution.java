package il.co.topq.report.model.execution;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ReportedExecution {

	private List<ReportedMachine> machines;

	public List<ReportedMachine> getMachines() {
		return machines;
	}

	public void setMachines(List<ReportedMachine> machines) {
		this.machines = machines;
	}

	public int addMachine(ReportedMachine machine) {
		if (machines == null) {
			machines = new ArrayList<ReportedMachine>();
		}
		machines.add(machine);
		return machines.indexOf(machine);
	}

	@JsonIgnore
	public ReportedMachine getLastMachine() {
		if (null == machines) {
			return null;
		}
		return machines.get(machines.size() - 1);

	}

}
