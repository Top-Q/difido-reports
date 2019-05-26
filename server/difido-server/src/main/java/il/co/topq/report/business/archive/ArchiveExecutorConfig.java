package il.co.topq.report.business.archive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Used for the Archive service
 */
@Configuration
public class ArchiveExecutorConfig {

	@Bean(name = "archiveExecutor")
	public ThreadPoolTaskExecutor getAsyncArchiveExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100000);
		executor.setThreadNamePrefix("AsyncArchiveActionQueue-");
		executor.initialize();
		return executor;
	}

}
