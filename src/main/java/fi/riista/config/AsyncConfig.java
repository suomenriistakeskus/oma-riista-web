package fi.riista.config;

import com.newrelic.api.agent.NewRelic;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurerSupport {

    @Resource
    private ThreadPoolTaskScheduler commonTaskScheduler;

    @Override
    public Executor getAsyncExecutor() {
        return new DelegatingSecurityContextAsyncTaskExecutor(commonTaskScheduler);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomUncaughtExceptionHandler();
    }

    public static class CustomUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
        private static final Logger LOG = LoggerFactory.getLogger(CustomUncaughtExceptionHandler.class);

        @Override
        public void handleUncaughtException(final Throwable ex, final Method method, final Object... params) {
            LOG.error(String.format("Unexpected error occurred invoking async method '%s'.", method), ex);

            NewRelic.noticeError(ex, false);
            Sentry.capture(ex);
        }
    }
}
