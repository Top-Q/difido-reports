package il.co.topq.difido;

import static java.nio.file.StandardCopyOption.*;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.test.TestDetails;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PersistenceUtils {

	private static final Logger log = Logger.getLogger(PersistenceUtils.class.getName());

	private static final String resourcesPath = "il.co.topq.difido.view/";

	public static final String EXECUTION_MODEL_FILE = "execution.js";

	public static final String TEST_DETAILS_MODEL_FILE = "test.js";

	public static final String TEST_DETAILS_HTML_FILE = "test.html";

	private static final ObjectMapper mapper = new ObjectMapper();

	private PersistenceUtils() {
		// static class
	}

	/**
	 * Search for the Jar files that holds all the HTML sources that needs to be
	 * extracted.
	 * 
	 * @return The jar file or null if fails to find one
	 */
	private static File findJarFileWithHtmlSources() {
		File jarFile = new File(Execution.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		if (jarFile.isFile()) {
			return jarFile;
		}
		File libFolder = new File("./lib");
		if (libFolder.exists()) {
			File[] files = libFolder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File file, String name) {
					if (name.contains("difido-server") && name.endsWith("jar")) {
						return true;
					}
					return false;
				}
			});
			if (files.length == 0) {
				log.warning("There are no files in the lib folder that can contain the HTML sources");
			}
			try {
				List<File> extractedFiles = ZipUtils.decopmerss(files[0].getAbsolutePath(),
						System.getProperty("java.io.tmpdir"), "lib/difido-reports-common");
				if (extractedFiles.isEmpty()) {
					log.warning("Failed find jar that contains HTML files");
				}
				return extractedFiles.get(0);
			} catch (Exception e) {
				log.warning("Failed to decompress jar that contains HTML files");
				return null;
			}

		}
		return null;

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

		final File jarFile = findJarFileWithHtmlSources();
		if (jarFile != null) {
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
				log.warning("Failed to copy HTML resources");
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
			log.warning("Found execution json file but failed reading it");
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

	public static TestDetails readTest(final File testSourceFolder) {
		final File testHtml = new File(testSourceFolder, TEST_DETAILS_MODEL_FILE);
		ObjectMapper mapper = new ObjectMapper();
		TestDetails test = null;
		try {
			final String json = FileUtils.readFileToString(testHtml);
			test = mapper.readValue(json.replaceFirst("var test = ", ""), TestDetails.class);
		} catch (IOException e) {
			log.warning("Found test details json file but failed reading it");
		}
		return test;
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

		final File tempFile = new File(testDestinationFolder, TEST_DETAILS_MODEL_FILE + "~");
		final File finalFile = new File(testDestinationFolder, TEST_DETAILS_MODEL_FILE);
		try {
			// We use temporary file and then we move it to the final to avoid
			// situations in which we try to read a file that is not completed
			String json = mapper.writeValueAsString(testDetails);
			json = "var test = " + json + ";";
			FileUtils.write(tempFile, json);
			Files.move(tempFile.toPath(), finalFile.toPath(), REPLACE_EXISTING);
		} catch (Exception e) {
			log.warning("Failed to write test details due to " + e.getMessage());
		}

	}
}
