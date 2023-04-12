package fi.riista.feature.gis.zone;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fi.riista.feature.huntingclub.area.HuntingClubAreaZoneFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessFailedAreaCalculationsJob {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessFailedAreaCalculationsJob.class);

    public static final int CONCURRENCY_LIMIT = 1;

    @Resource
    private HuntingClubAreaZoneFeature huntingClubAreaZoneFeature;

    private ThreadPoolExecutor executorService;

    @PostConstruct
    public void initExecutor() {
        // Pretty naming for worker threads
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("ProcessFailedAreaCalculationsJob-%d")
                .setDaemon(true)
                .build();

        this.executorService = new ThreadPoolExecutor(
                // Fixed size thread pool
                CONCURRENCY_LIMIT, CONCURRENCY_LIMIT,
                0L, TimeUnit.MILLISECONDS,
                // Tasks are always rejected when thread is not available for handoff
                new SynchronousQueue<>(),
                threadFactory,
                // Throw RejectedExecutionException when all threads are busy
                new ThreadPoolExecutor.AbortPolicy());
    }

    @PreDestroy
    public void shutdownExecutor() throws InterruptedException {
        executorService.shutdown();

        final boolean done = executorService.awaitTermination(15, TimeUnit.SECONDS);
        if (!done) {
            LOG.error("Some tasks failed to finish during Executor shutdown");
        }
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void processFailures() {
        try {
            huntingClubAreaZoneFeature.setTooLongProcessedZonesStatusFailed();

        } catch (final RejectedExecutionException ex) {
            LOG.warn("ProcessFailedAreaCalculationsJob thread pool is too busy...");

        } catch (final Exception ex) {
            LOG.error("Processing failed", ex);
        }
    }

}
