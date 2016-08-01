package il.co.topq.difido.model.execution;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "plannedTests" })
public class MachineNode extends NodeWithChildren<ScenarioNode> {

	/**
	 * The total number of tests that are planned to be executed on the machine
	 */
	@JsonProperty("plannedTests")
	private int plannedTests;

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

	/**
	 * Find test node recursively in all the children of the machine
	 * 
	 * @param uid
	 *            Unique id of the test node
	 * @return Test node with the requested uid or null if non was found.
	 */
	@JsonIgnore
	public TestNode findTestNodeById(String uid) {
		final List<ScenarioNode> allScenarios = getChildren(true);
		if (allScenarios == null) {
			return null;
		}
		for (Node scenario : allScenarios) {
			if (scenario == null || !(scenario instanceof ScenarioNode)) {
				continue;
			}
			final TestNode testNode = ((ScenarioNode) scenario).findTestByUid(uid);
			if (testNode != null) {
				return testNode;
			}
		}
		return null;
	}

	public int getPlannedTests() {
		return plannedTests;
	}

	public void setPlannedTests(int plannedTests) {
		this.plannedTests = plannedTests;
	}

}
