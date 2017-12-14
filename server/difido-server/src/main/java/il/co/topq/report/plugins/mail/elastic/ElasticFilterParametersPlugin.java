package il.co.topq.report.plugins.mail.elastic;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.NodeWithChildren;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionEndedEvent;
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
	private static final Logger logger = Logger.getLogger(ElasticFilterParametersPlugin.class);
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
		return "ElasticPlugin";
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
			machine.getChildren(true).forEach(scenarioNode -> {
				handleScenario(scenarioNode);
			});
		});
		logger.info("onExecutionEnded");
		//TODO:minupulate the metadata and do with it whatever we need to do
		
		ExecutionEndedEvent executionEndedEvent = new ExecutionEndedEvent(metadata);
		logger.info("ExecutionEndedEvent: " + executionEndedEvent);
		logger.info("esController: " + esController);
		esController.onExecutionEndedEvent(executionEndedEvent);
		
	}
	private void handleScenario(ScenarioNode scenarioNode) {
		scenarioNode.addScenarioProperty("scenario-node-property", "x");
		scenarioNode.getChildren().forEach(node -> {
			handleScenarioChildNode(node);
		});
		
	}
	@SuppressWarnings("unchecked")
	private void handleScenarioChildNode(Node node) {
		if (node instanceof TestNode) {
			TestNode testNode = (TestNode) node;
			//TODO:strip down parameters that we're not intrested in
			Map<String, String> parameters = testNode.getParameters();
			List<String> parametersToRemove = new LinkedList<>();
			parameters.keySet()
					  .stream()
					  .filter(key -> isParameterFilteredOutByNamingConvention(key))
					  .forEach(key -> parametersToRemove.add(key));
			
			parametersToRemove.forEach(key -> parameters.remove(key));
		} else if (node instanceof NodeWithChildren){
			//in case of nested scenarios this part will handle iterating over all the child nodes with recursion
			((NodeWithChildren<Node>) node).getChildren().forEach(child -> handleScenarioChildNode(node));
		}
	}
	private boolean isParameterFilteredOutByNamingConvention(String key) {
		return key.contains("ignore");
	}
}
