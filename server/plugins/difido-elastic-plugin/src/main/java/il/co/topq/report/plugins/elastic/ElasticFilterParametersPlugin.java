package il.co.topq.report.plugins.elastic;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.NodeWithChildren;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.plugins.ElasticPluginController;
import il.co.topq.report.plugins.ExecutionPlugin;

/**
 * this plugin will facilitate communication between the report server and elastic,
 * enabling us to insert custom behavior when writing to elastic.
 * Before using this plugin we should disable elastic in difido configuration
 * as having both elastic integration on and this plugin will result in writing to elastic twice.
 * 
 * @author angel
 *
 */
public class ElasticFilterParametersPlugin implements ExecutionPlugin {
	private ElasticPluginController esController;
	static {
		//private void addPropWithDefaultValue(ConfigProps configProp) {}
		//Configuration.INSTANCE.getClass().getDeclaredMethods();
	}
	
	public ElasticFilterParametersPlugin() {
		this.esController = new ElasticPluginController();
	}
	@Override
	public String getName() {
		return "ElasticFilterParamsPlugin";
	}

	@Override
	public void execute(List<ExecutionMetadata> metaDataList, String params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onExecutionEnded(ExecutionMetadata metadata) {
		if (metadata == null || metadata.getExecution() == null) return;
		metadata.getExecution().getMachines().forEach(machine -> {
			if (machine.getChildren() == null) return;
			machine.getChildren(true).forEach(child -> handleNode(child));
		});
		
		ExecutionEndedEvent executionEndedEvent = new ExecutionEndedEvent(metadata);
		esController.onExecutionEndedEvent(executionEndedEvent);		
	}
	@SuppressWarnings("unchecked")
	private void handleNode(Node node) {
		if (node instanceof TestNode) {
			TestNode testNode = (TestNode) node;
			Map<String, String> parameters = testNode.getParameters();
			Map<String, String> properties = testNode.getProperties();
			filterIgnoredValues(parameters);
			filterIgnoredValues(properties);
		} else if (node instanceof ScenarioNode){
			ScenarioNode scenarioNode = (ScenarioNode) node;
			Map<String, String> scenarioProperties = scenarioNode.getScenarioProperties();
			filterIgnoredValues(scenarioProperties);
			
		} if (node instanceof NodeWithChildren){
			((NodeWithChildren<Node>) node).getChildren().forEach(child -> handleNode(child));
		}
	}
	private void filterIgnoredValues(Map<String, String> parameters) {
		List<String> parametersToRemove = new LinkedList<>();
		parameters.keySet()
				  .stream()
				  .filter(key -> isParameterFilteredOutByNamingConvention(key))
				  .forEach(key -> parametersToRemove.add(key));
		parametersToRemove.forEach(key -> parameters.remove(key));
	}
	private boolean isParameterFilteredOutByNamingConvention(String key) {
		return key.toLowerCase().contains("ignore");
	}
}
