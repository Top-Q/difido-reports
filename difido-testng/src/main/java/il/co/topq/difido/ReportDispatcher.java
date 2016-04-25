package il.co.topq.difido;

import il.co.topq.difido.model.Enums.Status;

import java.io.File;

public interface ReportDispatcher {

	public abstract void logHtml(String title,Status status);

	public abstract void logHtml(String title, String message, Status status);

	public abstract void log(String title);

	public abstract void log(String title, Status status);

	public abstract void log(String title, String message);

	public abstract void log(String title, String message, Status status);

	public abstract void startLevel(String description);

	public abstract void endLevel();

	public abstract void step(String description);

	public abstract void addFile(File file, String description);

	public abstract void addImage(File file, String description);

	public abstract void addLink(String link, String description);

}