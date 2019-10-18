package fi.riista.config;

import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.Resource;

@Configuration
public class ExecutorConfig {
    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Bean
    public ThreadPoolTaskScheduler commonTaskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(runtimeEnvironmentUtil.isDevelopmentEnvironment() ? 1 : 20);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        return scheduler;
    }
}
