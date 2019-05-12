package il.co.topq.report;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TimeoutThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

	private static final long serialVersionUID = 3959474447997907574L;

	private final Logger log = LoggerFactory.getLogger(TimeoutThreadPoolTaskExecutor.class);

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			final Future<T> future = executor.submit(task);
//			try {
//				future.get(5, TimeUnit.SECONDS);
//			} catch (InterruptedException | ExecutionException e) {
//			} catch (TimeoutException e) {
//				log.error("Task computition exceeded timeout. Aborting task", e);
//			}
			return future;
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

}
