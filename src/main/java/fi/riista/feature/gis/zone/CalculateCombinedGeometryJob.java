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
public class CalculateCombinedGeometryJob {
    private static final Logger LOG = LoggerFactory.getLogger(CalculateCombinedGeometryJob.class);

    public static final int CONCURRENCY_LIMIT = 4;

    @Resource
    private HuntingClubAreaZoneFeature huntingClubAreaZoneFeature;

    private ThreadPoolExecutor executorService;

    @PostConstruct
    public void initExecutor() {
        // Pretty naming for worker threads
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("CalculateCombinedGeometryJob-%d")
                .setDaemon(true)
                .build();

        this.executorService = new ThreadPoolExecutor(
                // Fixed size thread pool
                CONCURRENCY_LIMIT, CONCURRENCY_LIMIT,
                0L, TimeUnit.MILLISECONDS,
                // Tasks are always rejected when thread is not available for handoff
                new SynchronousQueue<>(),
                threadFactory,
                // Throw RejectedExecutionExceptionwhen all threads are busy
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

    @Scheduled(fixedDelay = 2000)
    public void processPending() {
        try {
            huntingClubAreaZoneFeature.findZonesInStatusPending()
                    .stream()
                    .limit(executorService.getMaximumPoolSize())
                    .forEach(zoneId -> executorService.submit(createTask(zoneId)));

        } catch (final RejectedExecutionException ex) {
            LOG.warn("CalculateCombinedGeometryJob thread pool is too busy...");

        } catch (final Exception ex) {
            LOG.error("Processing failed", ex);
        }
    }

    @Nonnull
    private Callable<Void> createTask(final Long zoneId) {
        return () -> {
            try {
                huntingClubAreaZoneFeature.startProcessing(zoneId);

            } catch (final Exception e) {
                LOG.error("Uncaught exception while processing", e);
            }
            return null;
        };
    }
}
