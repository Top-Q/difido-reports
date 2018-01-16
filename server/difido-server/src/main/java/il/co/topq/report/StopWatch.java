package il.co.topq.report;

import org.slf4j.Logger;

public class StopWatch {

	final private org.springframework.util.StopWatch stopWatch;
	final private Logger log;

	public StopWatch(Logger log) {
		if (log == null) {
			throw new NullPointerException("Log is null");
		}
		stopWatch = new org.springframework.util.StopWatch();
		stopWatch.setKeepTaskList(false);
		this.log = log;
	}
	
	/**
	 * Builder method
	 * 
	 * @param log
	 * @return
	 */
	public static StopWatch newStopWatch(Logger log) {
		return new StopWatch(log);
	}

	public StopWatch start(String taskName) {
		if (!log.isTraceEnabled()){
			return this;
		}
		if (stopWatch.isRunning()){
			stopWatch.stop();
		}
		try {
			stopWatch.start(taskName);
		} catch (IllegalStateException e) {
			log.warn("Exception while trying to start stopwatch " + e.getMessage());
		}
		return this;
	}

	public StopWatch stop() {
		if (!log.isTraceEnabled()){
			return this;
		}

		try {
			stopWatch.stop();
		} catch (IllegalStateException e) {
			log.warn("Exception while trying to stop stopwatch " + e.getMessage());
		}
		return this;
	}

	public StopWatch log() {
		if (!log.isTraceEnabled()){
			return this;
		}

		log.trace("Elapsed time for task '" + stopWatch.getLastTaskInfo().getTaskName() + "' is: "
				+ stopWatch.getLastTaskTimeMillis() + " mills");
		return this;
	}

	public StopWatch stopAndLog() {
		if (!log.isTraceEnabled()){
			return this;
		}
		stop();
		log();
		return this;
	}

}
