package il.co.topq.difido;

import il.co.topq.difido.model.Enums.Status;

import java.io.File;

import org.testng.IInvokedMethod;
import org.testng.ITestResult;

public interface ReportDispatcher {

	void logHtml(String title, Status status);

	void logHtml(String title, String message, Status status);

	void log(String title);

	void log(String title, Status status);

	void log(String title, String message);

	void log(String title, String message, Status status);

	void startLevel(String description);

	void endLevel();

	void step(String description);

	void addFile(File file, String description);

	void addImage(File file, String description);

	void addLink(String link, String description);

	void addTestProperty(String name, String value);

	void addRunProperty(String name, String value);

}