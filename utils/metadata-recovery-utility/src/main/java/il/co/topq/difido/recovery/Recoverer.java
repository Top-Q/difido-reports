package il.co.topq.difido.recovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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

public class Recoverer {

	private Logger log = LoggerFactory.getLogger(Recoverer.class);

	private final File docRootFolder;

	private final File metaFile;

	private final File allowedPropsFile;

	public Recoverer(File docRootFolder, File metaFile, File allowedPropsFile) {
		super();
		this.docRootFolder = docRootFolder;
		this.metaFile = metaFile;
		this.allowedPropsFile = allowedPropsFile;
	}

	public void recover() {
		final File reportsFolder = new File(docRootFolder, "reports");
		if (!reportsFolder.exists() || !reportsFolder.isDirectory()) {
			log.error("Report folder does not exists in '" + docRootFolder.getAbsolutePath() + "' folder");
			System.exit(1);
		}
		log.debug("Parsing scenario properties file");
		final List<String> allowedProps = parseAllowedProperties();
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
			ExecutionMetadata metadata = extractMetadata(execution, allowedProps);
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

	private List<String> parseAllowedProperties() {
		final List<String> props = new ArrayList<>();
		if (null == allowedPropsFile || !allowedPropsFile.exists()) {
			log.debug("No allowed properties were specified. Using all properties");
			return props;
		}
		try (Scanner scanner = new Scanner(allowedPropsFile);) {
			while (scanner.hasNextLine()) {
				props.add(scanner.nextLine().trim());
			}
		} catch (FileNotFoundException e) {
			log.error("Failed to parse allowed properties file", e);
		}
		return props;
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

	private ExecutionMetadata extractMetadata(Execution execution, List<String> allowedProps) {
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
		if (execution != null && execution.getMachines() != null) {
			for (MachineNode machine : execution.getMachines()) {
				numOfMachines++;
				if (null == machine) {
					continue;
				}
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
									time = EnhancedDateTimeConverter.fromTimeString(test.getTimestamp() + ":00")
											.toDateObject();
									date = EnhancedDateTimeConverter.fromReverseDateString(test.getDate())
											.toDateObject();
									timestamp = test.getDate() + " " + test.getTimestamp();
								} catch (Exception e) {
									log.error("Failed to parse date or time from test " + test.toString(), e);
								}

							}
						}
					}
				}
			}
		} else {
			log.error("Execution or machines are null");
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
		if (scenarioProperties != null && !scenarioProperties.isEmpty()) {
			if (allowedProps != null && !allowedProps.isEmpty()) {
				for (String propName : scenarioProperties.keySet()) {
					if (allowedProps.contains(propName)) {
						executionMetaData.addProperty(propName, scenarioProperties.get(propName));
					}
				}

			} else {
				executionMetaData.setProperties(scenarioProperties);
			}
		}
		return executionMetaData;
	}

}
