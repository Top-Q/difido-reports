package il.co.topq.difido.model.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author agmon
 * 
 */
@JsonPropertyOrder({ "scenarioProperties"})
public class ScenarioNode extends NodeWithChildren<Node> {

	@JsonProperty("scenarioProperties")
	private Map<String, String> scenarioProperties;

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
		scenarioNodeCopy.setScenarioProperties(new HashMap<String,String>(aScenarioNode.getScenarioProperties()));
		return scenarioNodeCopy;
	}

	@JsonIgnore
	public void addScenarioProperty(String key, String property) {
		if (null == scenarioProperties) {
			scenarioProperties = new HashMap<String, String>();
		}
		scenarioProperties.put(key, property);
		if (getParent() instanceof ScenarioNode){
			((ScenarioNode)getParent()).addScenarioProperty(key, property);
		}
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

	/**
	 * Finds a test node with the specified UID
	 * 
	 * @param uid
	 *            UID if the requested test
	 * @return test node with the specified UID or null if none was found
	 */
	public TestNode findTestByUid(String uid) {
		if (uid == null || uid.isEmpty()) {
			throw new IllegalArgumentException("UID can't be null or empty");
		}
		List<Node> children = getChildren();
		if (children == null) {
			return null;
		}
		for (Node node : children) {
			if (!(node instanceof TestNode)) {
				continue;
			}
			final TestNode testNode = (TestNode) node;
			if (testNode.getUid() != null && testNode.getUid().equals(uid.trim())) {
				return testNode;
			}
		}
		return null;
	}

	public Map<String, String> getScenarioProperties() {
		return scenarioProperties;
	}

	private void setScenarioProperties(Map<String, String> scenarioProperties) {
		this.scenarioProperties = scenarioProperties;
	}

}
