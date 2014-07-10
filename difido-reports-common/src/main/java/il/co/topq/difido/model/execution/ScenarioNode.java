package il.co.topq.difido.model.execution;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author agmon
 * 
 */
public class ScenarioNode extends NodeWithChildren<Node> {

	public ScenarioNode() {
	}

	public ScenarioNode(String name) {
		super(name);
	}

	/**
	 * Copy constructor
	 * 
	 * @param aScenarioNode
	 * @return
	 */
	@JsonIgnore
	public static ScenarioNode newInstance(ScenarioNode aScenarioNode) {
		ScenarioNode scenarioNodeCopy = new ScenarioNode(aScenarioNode.getName());
		scenarioNodeCopy.setChildren(aScenarioNode.getChildren());
		scenarioNodeCopy.setParent(aScenarioNode.getParent());
		scenarioNodeCopy.setStatus(aScenarioNode.getStatus());
		return scenarioNodeCopy;
	}

	@JsonIgnore
	@Override
	public void addChild(Node node) {
		super.addChild(node);
		if (node instanceof ScenarioNode) {
			// We need to inform the root machine about the scenario
			MachineNode machine = findMachine();
			if (machine != null) {
				machine.addSubScenario((ScenarioNode) node);
			}
		}
	}

	/**
	 * Searched in the parents of all the ancestors for the machine that holds
	 * the scenarios
	 * 
	 * @return The root machine object or null if failed to find.
	 */
	private MachineNode findMachine() {
		Node node = this;
		while (!(node.getParent() instanceof MachineNode)) {
			node = node.getParent();
			if (null == node) {
				return null;
			}
		}
		return (MachineNode) node.getParent();
	}

}
