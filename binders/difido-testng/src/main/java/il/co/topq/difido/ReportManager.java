package il.co.topq.difido;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

public class ReportManager implements ReportDispatcher {

	private static ReportManager instance;

	private List<Reporter> reporters;

	private ReportManager() {
		
		File difidoPropertiesFile = new File("difido.properties");

		if (difidoPropertiesFile.exists()) {
			
			try {
				List<Reporter> reportersFromFile = loadReportersFromPropertiesFile(difidoPropertiesFile);
				
				if (reportersFromFile != null && reportersFromFile.size() > 0) {
					reporters = reportersFromFile;
				}
				else {
					print("WARNING: Error loading reporters from difido.properties file. Using default reporters.");
					addDefaultReporters();
				}
			}
			catch (Exception e) {
				print("WARNING: Error loading reporters from difido.properties file. Using default reporters.");
				addDefaultReporters();
			}
		}
		else {
			print("WARNING: difido.properties file wasn't found. Using default reporters.");
			addDefaultReporters();
		}
	}

	public static ReportManager getInstance() {
		if (null == instance) {
			instance = new ReportManager();
		}
		return instance;
	}

	void onTestStart(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestStart(result);
		}

	}

	@Override
	public void logHtml(String title, Status status) {
		log(title, null, status, ElementType.html);
		
	}

	@Override
	public void logHtml(String title, String message, Status status) {
		log(title, message, status, ElementType.html);		
	}

	
	public void log(String title, String message, Status status, ElementType type) {
		for (Reporter reporter : reporters) {
			reporter.log(title, message, status, type);
		}
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#log(java.lang.String)
	 */
	@Override
	public void log(String title) {
		log(title, null, Status.success, ElementType.regular);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#log(java.lang.String, il.co.topq.difido.model.Enums.Status)
	 */
	@Override
	public void log(String title, Status status) {
		log(title, null, status, ElementType.regular);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#log(java.lang.String, java.lang.String)
	 */
	@Override
	public void log(String title, String message) {
		log(title, message, Status.success, ElementType.regular);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#log(java.lang.String, java.lang.String, il.co.topq.difido.model.Enums.Status)
	 */
	@Override
	public void log(String title, String message, Status status) {
		log(title, message, status, ElementType.regular);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#startLevel(java.lang.String)
	 */
	@Override
	public void startLevel(String description) {
		log(description, null, Status.success, ElementType.startLevel);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#endLevel()
	 */
	@Override
	public void endLevel() {
		log(null, null, Status.success, ElementType.stopLevel);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#step(java.lang.String)
	 */
	@Override
	public void step(String description) {
		log(description, null, Status.success, ElementType.step);
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#addFile(java.io.File, java.lang.String)
	 */
	@Override
	public void addFile(File file, String description) {
		for (Reporter reporter : reporters) {
			reporter.addFile(file);
			if (null == description){
				reporter.log(file.getName(), file.getName(), Status.success, ElementType.lnk);
			} else {
				reporter.log(description, file.getName(), Status.success, ElementType.lnk);
			}
		}
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#addImage(java.io.File, java.lang.String)
	 */
	@Override
	public void addImage(File file, String description) {
		for (Reporter reporter : reporters) {
			reporter.addFile(file);
			reporter.log(description, file.getName(), Status.success, ElementType.img);
		}
	}

	/* (non-Javadoc)
	 * @see il.co.topq.difido.ReportDispatcher#addLink(java.lang.String, java.lang.String)
	 */
	@Override
	public void addLink(String link, String description) {
		for (Reporter reporter : reporters) {
			reporter.log(description, link, Status.success, ElementType.img);
		}
	}

	void onTestSuccess(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestSuccess(result);
		}

	}

	void onTestFailure(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestFailure(result);
		}
	}

	void onTestSkipped(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestSkipped(result);
		}

	}

	void onStart(ITestContext context) {
		for (Reporter reporter : reporters) {
			reporter.onStart(context);
		}
	}

	void onFinish(ITestContext context) {
		for (Reporter reporter : reporters) {
			reporter.onFinish(context);
		}

	}

	public void onStart(ISuite suite) {
		for (Reporter reporter : reporters) {
			reporter.onStart(suite);
		}
	}

	public void onFinish(ISuite suite) {
		for (Reporter reporter : reporters) {
			reporter.onFinish(suite);
		}
	}

	@Override
	public void addTestProperty(String name, String value) {
		for (Reporter reporter : reporters) {
			reporter.addTestProperty(name,value);
		}
	}

	@Override
	public void addRunProperty(String name, String value) {
		for (Reporter reporter : reporters) {
			reporter.addRunProperty(name,value);
		}
		
	}

	private List<Reporter> loadReportersFromPropertiesFile(File difidoPropertiesFile) throws Exception {
		
		Properties prop = new Properties();
		prop.load(new FileInputStream(difidoPropertiesFile));
		String reporterClassNames = prop.getProperty("reporter.classes");
		
		if (reporterClassNames != null && !reporterClassNames.equals("")) {
			
			String[] reporterClasseNamesSplit = reporterClassNames.split(";");
			ArrayList<Reporter> reporters = new ArrayList<>();
			
			for (String className : reporterClasseNamesSplit) {
				
				try {
					Class<?> clazz = Class.forName(className);
					Constructor<?> constructor = clazz.getConstructor();
					Reporter reporter = (Reporter) constructor.newInstance();
					reporters.add(reporter);
					print("Added reporter: " + className);
				}
				catch (Exception e) {
					print("WARNING: Error loading reporter class: " + className);
				}
			}
			
			return reporters;
		}
		else {
			return null;
		}
	}
	
	private void addDefaultReporters() {
		print("Adding default reporters: LocalDifidoReporter, RemoteDifidoReporter");
		reporters = new ArrayList<Reporter>();
		reporters.add(new LocalDifidoReporter());
		reporters.add(new RemoteDifidoReporter());
	}
	
	public void addReporter(Reporter reporter) {
		reporters.add(reporter);
	}
	
	private void print(String message) {
		System.out.println("[Difido] " + message);
	}
}
