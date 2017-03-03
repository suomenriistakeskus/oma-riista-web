package fi.riista.feature.harvestpermit.area;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class ProcessPendingHarvestPermitAreaJob {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessPendingHarvestPermitAreaJob.class);

    private static final int CONCURRENCY_LIMIT = 1;
    private static final int TIMEOUT_MINUTES = 15;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private ProcessHarvestPermitAreaZoneFeature processHarvestPermitAreaZoneFeature;

    private ExecutorService executorService;

    @PostConstruct
    public void initExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("ProcessHarvestPermitAreaJob-%d")
                .setDaemon(true)
                .build();

        this.executorService = new ThreadPoolExecutor(
                CONCURRENCY_LIMIT, CONCURRENCY_LIMIT, 30, TimeUnit.SECONDS,
                // Task queue with zero buffer
                new SynchronousQueue<>(), threadFactory,
                // Executor will silently discard tasks when concurrency limit is reached
                new ThreadPoolExecutor.DiscardPolicy());
    }

    @PreDestroy
    public void shutdownExecutor() throws InterruptedException {
        executorService.shutdown();

        final boolean done = executorService.awaitTermination(15, TimeUnit.SECONDS);
        if (!done) {
            LOG.error("Some tasks failed to finish during Executor shutdown");
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void processPending() {
        getPendingZoneIds(CONCURRENCY_LIMIT).stream()
                .map(zoneId -> new FutureTask<>(createRunnable(zoneId), true))
                .map(t -> executorService.submit(t))
                .forEach(task -> {
                    try {
                        task.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
                    } catch (TimeoutException e) {
                        task.cancel(true);
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error("Error while processing", e);
                    }
                });
    }

    @Nonnull
    private Runnable createRunnable(final Long pendingZoneId) {
        return () -> {
            try {
                processHarvestPermitAreaZoneFeature.startProcessing(pendingZoneId);

            } catch (Exception e) {
                LOG.error("Uncaught exception while processing", e);
            }
        };
    }

    private List<Long> getPendingZoneIds(final int limit) {
        final JpaSort sort = new JpaSort(Sort.Direction.ASC, HarvestPermitArea_.statusTime);
        final PageRequest pageRequest = new PageRequest(0, limit, sort);
        final DateTime processingSince = DateTime.now().minusHours(1);
        return harvestPermitAreaRepository.findInStatusPendingOrProcessingTooLong(processingSince, pageRequest);
    }
}
