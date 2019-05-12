package il.co.topq.report.business;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * This class is highly dependent on the AsyncExecutor configuration that is
 * done in the Application class. The purpose of this class is to provide a none
 * blocking queue for the write operations that are done to the file system.
 *
 * @author Itai Agmon
 */
@Service
public class AsyncActionQueue {

	private final Logger log = LoggerFactory.getLogger(AsyncActionQueue.class);

	/**
	 * The maximum time in seconds task have to live from start of execution
	 * before it will be killed.
	 */
	private static final long TASK_TIMEOUT_IN_SECONDS = 5;

	/**
	 * 
	 * @param taskDescription
	 *            - Task description to be printed in cases there is a need to
	 *            kill the task.
	 * @param action
	 *            - The task to perform
	 */
	@Async
	public void addAction(final String taskDescription, final AsyncAction action) {
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<?> future = executorService.submit(() -> {
			action.execute();
		});
		try {
			future.get(TASK_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.error("Task '" + taskDescription + "' failed due to " + e.getMessage(), e);
		} catch (TimeoutException e) {
			log.error("Task '" + taskDescription + "' failed due to timeout of " + TASK_TIMEOUT_IN_SECONDS + " seconds",
					e);
		}
	}

	public interface AsyncAction {
		public void execute();
	}

}
