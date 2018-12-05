	package il.co.topq.report;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application extends SpringBootServletInitializer implements AsyncConfigurer,InfoContributor {

	ThreadPoolTaskExecutor executor;
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}


	/**
	 * Configuration of the async executor. This is used for writing to the file
	 * system and it is very important that there will be no more then one
	 * thread in the pool.
	 */
	@Override
	public Executor getAsyncExecutor() {
		executor = new ThreadPoolTaskExecutor();
		// Do not change the number of threads here
		executor.setCorePoolSize(1);
		// Do not change the number of threads here
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(100000);
		executor.setThreadNamePrefix("AsyncActionQueue-");
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		// Unused
		return null;
	}

	/**
	 * Info about the server that can be retrieved using the
	 * http://<host>:<port>/info request
	 */
	@Override
	public void contribute(org.springframework.boot.actuate.info.Info.Builder builder) {
		if (null == executor) {
			return;
		}
		Map<String, Integer> queueDetails = new HashMap<>();
		queueDetails.put("active count", executor.getActiveCount());
		queueDetails.put("pool size", executor.getPoolSize());
		queueDetails.put("max pool size", executor.getMaxPoolSize());
		builder.withDetail("asyncActionQueue", queueDetails).build();
		
	}

}