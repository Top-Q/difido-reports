package il.co.topq.report.plugins.elastic;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.NodeWithChildren;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.ExecutionMetadata;
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
public class ElasticSplitSubTestsPlugin implements ExecutionPlugin {
	private static final Logger logger = Logger.getLogger(ElasticSplitSubTestsPlugin.class);
	private ElasticPluginController esController;
	private static final String EXECUTION_HTML_PATTERN    = "reports/%s/index.html";
	private static final String EXECUTION_JS_FILE_PATTERN = "docRoot\\reports\\%s\\tests\\test_%s";
	static {
		//private void addPropWithDefaultValue(ConfigProps configProp) {}
		//Configuration.INSTANCE.getClass().getDeclaredMethods();
	}
	
	public ElasticSplitSubTestsPlugin() {
		this.esController = new ElasticPluginController();
	}
	@Override
	public String getName() {
		return "ElasticSplitSubTestsPlugin";
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
				handleNode(scenarioNode, metadata.getFolderName());
			});
		});
	}
		
		@SuppressWarnings("unchecked")
		private void handleNode(Node node, String executionId) {
			if (node instanceof TestNode) {
				TestNode testNode = (TestNode) node;
				String testFolderPath = String.format(EXECUTION_JS_FILE_PATTERN, executionId, testNode.getUid());
				File testFolder = new File(testFolderPath);
				testFolder.getAbsolutePath();
				TestDetails testDetails = PersistenceUtils.readTest(testFolder);
//				testDetails.getReportElements().forEach(reportElement -> {
//					reportElement.getType() == ElementType.
//				});
				//TODO:use test details to find sub tests and report them as individual tests

			} else if (node instanceof NodeWithChildren){
				//in case of nested scenarios this part will handle iterating over all the child nodes with recursion
				((NodeWithChildren<Node>) node).getChildren()
											   .forEach(child -> handleNode(child, executionId));
			}
		}

		
//		ExecutionEndedEvent executionEndedEvent = new ExecutionEndedEvent(metadata);
//		esController.onExecutionEndedEvent(executionEndedEvent);

}
