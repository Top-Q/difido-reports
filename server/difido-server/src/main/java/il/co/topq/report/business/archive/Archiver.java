package il.co.topq.report.business.archive;

/**
 * Interface for services that are responsible for pulling executions from
 * remote Difido server to archive them
 * 
 * @author Itai Agmon
 *
 */
public interface Archiver {

	/**
	 * Starting the archive process. <br>
	 * 1. Initialize the local reports folder if it is not exists.<br>
	 * 2. Get all the remote executions<br>
	 * 3. Archive old and finished executions <br>
	 * 
	 */
	void archive();

}
