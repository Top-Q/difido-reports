package il.co.topq.difido;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class PersistenceUtils {

	private static final Logger log = Logger.getLogger(PersistenceUtils.class.getName());

	private static final String resourcesPath = "il.co.topq.difido.view/";

	private static final String EXECUTION_MODEL_FILE = "execution.js";

	private static final String TEST_DETAILS_MODEL_FILE = "test.js";

	private static final String TEST_DETAILS_HTML_FILE = "test.html";

	private static final ObjectMapper mapper = new ObjectMapper();

	private PersistenceUtils() {
		// static class
	}

	/**
	 * Copy all the HTML and other necessary files to the specified destination
	 * folder
	 * 
	 * @param destinationFolder
	 */
	public static void copyResources(File destinationFolder) {
		if (!destinationFolder.exists()) {
			if (!destinationFolder.mkdir()) {
				log.warning("Failed to create log folder " + destinationFolder.getAbsolutePath());
				return;
			}
		}

		final File jarFile = new File(Execution.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		if (jarFile.isFile()) {
			try {
				ZipUtils.decopmerss(jarFile.getAbsolutePath(), destinationFolder.getAbsolutePath(), resourcesPath);
			} catch (Exception e) {
				log.warning("Failed to copy HTML resources");
				return;
			}

		} else {
			URL resourceFiles = Execution.class.getClassLoader().getResource(resourcesPath);
			try {
				File files = new File(resourceFiles.toURI());
				FileUtils.copyDirectory(files, destinationFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * Read the JSon file from the specified sourceFolder and deserialize it to
	 * execution object
	 * 
	 * @param sourceFolder
	 * @return
	 */
	public static Execution readExecution(File sourceFolder) {
		File executionJson = new File(sourceFolder, EXECUTION_MODEL_FILE);
		if (!executionJson.exists()) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		Execution execution = null;
		try {
			final String json = FileUtils.readFileToString(executionJson);
			execution = mapper.readValue(json.replaceFirst("var execution = ", ""), Execution.class);
		} catch (IOException e) {
			log.warning("Found execution json file but failed to reading it");
		}
		return execution;
	}

	/**
	 * Serialized the specified execution to JSon and copy it to the destination
	 * folder.
	 * 
	 * @param execution
	 * @param destinationFolder
	 */
	public static void writeExecution(Execution execution, File destinationFolder) {
		try {
			File executionModelFile = new File(destinationFolder, EXECUTION_MODEL_FILE);
			if (executionModelFile.exists()) {
				File executionModelFileBackup = new File(destinationFolder, EXECUTION_MODEL_FILE + ".backup");
				if (executionModelFileBackup.exists()) {
					executionModelFileBackup.delete();
				}
				executionModelFile.renameTo(executionModelFileBackup);
			}
			String json = mapper.writeValueAsString(execution);
			json = "var execution = " + json + ";";
			FileUtils.write(executionModelFile, json);
		} catch (Exception e) {
			log.warning("Failed to write html report due to " + e.getMessage());
		}

	}

	/**
	 * Converts the test details to JSon file and copy it to the HTML report
	 * folder.
	 * 
	 * @param testDetails
	 *            Test details data
	 * @param currentReportFolder
	 *            The root folder of the HTML reports. It is needed in order to
	 *            copy the test HTML file
	 * @param testDestinationFolder
	 *            The folder contains the test details.
	 */
	public static void writeTest(TestDetails testDetails, File currentReportFolder, File testDestinationFolder) {
		final File testHtml = new File(testDestinationFolder, TEST_DETAILS_HTML_FILE);
		if (!testHtml.exists()) {
			try {
				FileUtils.copyFile(new File(currentReportFolder, TEST_DETAILS_HTML_FILE), testHtml);
			} catch (IOException e) {
				log.warning("Failed to create HTML test details file due to " + e.getMessage());
			}
		}
		try {
			String json = mapper.writeValueAsString(testDetails);
			json = "var test = " + json + ";";
			FileUtils.write(new File(testDestinationFolder, TEST_DETAILS_MODEL_FILE), json);
		} catch (Exception e) {
			log.warning("Failed to write test details due to " + e.getMessage());
		}

	}
}
