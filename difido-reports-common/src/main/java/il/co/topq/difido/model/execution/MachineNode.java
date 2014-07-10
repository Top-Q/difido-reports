package il.co.topq.difido.model.execution;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class MachineNode extends NodeWithChildren<ScenarioNode> {

	@JsonIgnore
	private List<ScenarioNode> allScenarios;

	public MachineNode() {
	}

	public MachineNode(String name) {
		super(name);
	}

	@JsonIgnore
	@Override
	public void addChild(ScenarioNode scenario) {
		super.addChild(scenario);
		addSubScenario(scenario);
	}

	@JsonIgnore
	public void addSubScenario(ScenarioNode scenario) {
		if (null == allScenarios) {
			allScenarios = new ArrayList<ScenarioNode>();
		}
		allScenarios.add(scenario);
	}

	@JsonIgnore
	public List<ScenarioNode> getAllScenarios() {
		return allScenarios;
	}
	

}
