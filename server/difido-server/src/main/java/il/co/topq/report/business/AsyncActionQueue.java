package il.co.topq.report.business;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * This class is highly dependent on the AsyncExecutor configuration that is
 * done in the Application class. The purpose of this class is to provide a none
 * blocking queue for the write operations that are done to the file system.
 *
 */
@Service
public class AsyncActionQueue {
	
	@Async
	public void addAction(AsyncAction action) {
		action.execute();
	}

	public interface AsyncAction {
		public void execute();
	}

}
