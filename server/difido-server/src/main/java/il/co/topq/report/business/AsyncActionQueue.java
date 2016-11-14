package il.co.topq.report.business;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncActionQueue {

	@Async
	public void addAction(AsyncAction action) {
		action.execute();
	}

	public abstract static class AsyncAction {
		public abstract void execute();
	}

}
