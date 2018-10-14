package il.co.topq.difido.retriever;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.business.execution.ExecutionMetadata;

public class Retriever {
	
	private Logger log = LoggerFactory.getLogger(Retriever.class);
	
	private final File docRootFolder;
	
	private final File metaFile;

	public Retriever(File docRootFolder, File metaFile) {
		super();
		this.docRootFolder = docRootFolder;
		this.metaFile = metaFile;
	}
	
	public void retrieve() { 
		final File reportsFolder = new File(docRootFolder, "reports");
		if (!reportsFolder.exists() || !reportsFolder.isDirectory()) {
			log.error("Report folder does not exists in '" + docRootFolder.getAbsolutePath() + "' folder");
			System.exit(1);
		}
		log.debug("Collecting reports from " + reportsFolder.getAbsolutePath());
		List<Path> foldersList = null;
		try {
			// @formatter:off
			foldersList = Files.walk(reportsFolder.toPath(), 1)
			.filter(Files::isDirectory)			
			.collect(Collectors.toList());
			foldersList.remove(0);
			// @formatter:on

		} catch (IOException e) {
			log.error("Failed to collect report folder names from " + docRootFolder.getAbsolutePath() + "'", e);
			System.exit(1);
		}
		if (foldersList.isEmpty()) {
			log.info("No report folders were found in '" + docRootFolder.getAbsolutePath() + "'. Nothing to do");
			System.exit(0);
		}
		log.debug(foldersList.toString());
		Map<Integer, ExecutionMetadata> executionsMap = new HashMap<>();
		for (Path reportPath : foldersList) {
			log.debug("About to parse folder " + reportPath);
			final int id = extractExecutionId(reportPath);
			log.debug("Parsed execution id is " + id);
			Execution execution = PersistenceUtils.readExecution(reportPath.toFile());
			ExecutionMetadata metadata = extractMetadata(execution);
			metadata.setId(id);
			metadata.setFolderName(reportPath.getFileName().toString());
			metadata.setUri("reports/" + reportPath.getFileName().toString() + "/index.html");
			executionsMap.put(id, metadata);
		}
		try {
			new ObjectMapper().writeValue(metaFile, executionsMap);

		} catch (Exception e) {
			log.error("Failed writing metadata map to file", e);
			System.exit(1);
		}
		log.info("Finished successfully creating meta.json file in " + metaFile.getAbsolutePath());
	
	}
	
	private int extractExecutionId(Path reportPath) {
		String folderName = reportPath.getFileName().toString();
		String idAsString = folderName.substring(folderName.indexOf("_") + 1);

		try {
			return Integer.parseInt(idAsString);
		} catch (NumberFormatException e) {
			log.error("Failed to parse id from string '" + idAsString + "'", e);
			return -1;
		}

	}

	private ExecutionMetadata extractMetadata(Execution execution) {
		int numOfTests = 0;
		int numOfSuccessfulTests = 0;
		int numOfFailedTests = 0;
		int numOfTestsWithWarnings = 0;
		int numOfMachines = 0;
		long duration = 0;
		Date date = null;
		Date time = null;
		String timestamp = null;
		Map<String, String> scenarioProperties = null;

		final ExecutionMetadata executionMetaData = new ExecutionMetadata();
		boolean firstTest = true;
		boolean firstScenario = true;
		for (MachineNode machine : execution.getMachines()) {
			numOfMachines++;
			final List<ScenarioNode> scenarios = machine.getChildren();
			if (null == scenarios) {
				continue;
			}
			for (ScenarioNode scenario : scenarios) {
				if (firstScenario) {
					firstScenario = false;
					scenarioProperties = scenario.getScenarioProperties();
				}
				for (Node node : scenario.getChildren(true)) {
					if (node instanceof TestNode) {
						numOfTests++;
						switch (node.getStatus()) {
						case success:
							numOfSuccessfulTests++;
							break;
						case error:
						case failure:
							numOfFailedTests++;
							break;
						case warning:
							numOfTestsWithWarnings++;
						default:
							break;
						}
						TestNode test = (TestNode) node;
						duration += test.getDuration();
						if (firstTest) {
							firstTest = false;
							try {
								time = EnhancedDateTimeConverter.fromTimeString(test.getTimestamp() + ":00").toDateObject();
								date = EnhancedDateTimeConverter.fromReverseDateString(test.getDate()).toDateObject();
								timestamp = test.getDate() + " " + test.getTimestamp();
							} catch (Exception e) {
								log.error("Failed to parse date or time from test " + test.toString(), e);
							}

						}
					}
				}
			}
		}
		executionMetaData.setNumOfTests(numOfTests);
		executionMetaData.setNumOfFailedTests(numOfFailedTests);
		executionMetaData.setNumOfSuccessfulTests(numOfSuccessfulTests);
		executionMetaData.setNumOfTestsWithWarnings(numOfTestsWithWarnings);
		executionMetaData.setNumOfMachines(numOfMachines);
		executionMetaData.setDuration(duration);
		if (date != null) {
			executionMetaData.setDate(EnhancedDateTimeConverter.fromDateObject(date).toDateString());
		}
		if (time != null) {
			executionMetaData.setTime(EnhancedDateTimeConverter.fromDateObject(time).toTimeString());
		}
		executionMetaData.setTimestamp(timestamp);
		executionMetaData.setProperties(scenarioProperties);
		return executionMetaData;
	}

	
	
	
	
}
