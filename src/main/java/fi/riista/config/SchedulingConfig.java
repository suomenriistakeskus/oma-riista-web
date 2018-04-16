package fi.riista.config;

import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.StandardDatabase;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;

@EnableScheduling
@AmazonDatabase
@StandardDatabase
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    @Resource
    private ThreadPoolTaskScheduler commonTaskScheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(commonTaskScheduler);
    }

}
