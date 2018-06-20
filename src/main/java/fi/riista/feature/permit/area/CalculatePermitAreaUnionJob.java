package fi.riista.feature.permit.area;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.UncheckedTimeoutException;
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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class CalculatePermitAreaUnionJob {
    private static final Logger LOG = LoggerFactory.getLogger(CalculatePermitAreaUnionJob.class);

    private static final int CONCURRENCY_LIMIT = 1;
    private static final int TIMEOUT_MINUTES = 15;

    private static ThreadPoolExecutor createWorkExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("ProcessHarvestPermitAreaJob-%d")
                .setDaemon(true)
                .build();

        return new ThreadPoolExecutor(
                CONCURRENCY_LIMIT, CONCURRENCY_LIMIT, 30, TimeUnit.SECONDS,
                // Task queue with zero buffer
                new SynchronousQueue<>(), threadFactory,
                // Notify rejection to caller
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private CalculatePermitAreaUnionFeature processHarvestPermitAreaZoneFeature;

    private ThreadPoolExecutor executorService;
    private SimpleTimeLimiter timeLimiter;

    @PostConstruct
    public void initExecutor() {
        this.executorService = createWorkExecutor();
        this.timeLimiter = SimpleTimeLimiter.create(this.executorService);
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
        final Optional<Long> optional = getFirstPendingZoneId();

        if (!optional.isPresent()) {
            return;
        }

        optional.ifPresent(zoneId -> {
            try {
                timeLimiter.callWithTimeout(createTask(zoneId), TIMEOUT_MINUTES, TimeUnit.MINUTES);
            } catch (UncheckedTimeoutException te) {
                LOG.info("Task for zoneId={} caused timeout while processing", zoneId);

            } catch (RejectedExecutionException re) {
                LOG.warn("Work queue is full...");

            } catch (Exception ex) {
                LOG.error("Processing failed for zoneId=" + zoneId, ex);
            }
        });
    }

    @Nonnull
    private Callable<Void> createTask(final Long pendingZoneId) {
        return () -> {
            try {
                processHarvestPermitAreaZoneFeature.startProcessing(pendingZoneId);

            } catch (Exception e) {
                LOG.error("Uncaught exception while processing", e);
            }
            return null;
        };
    }

    private Optional<Long> getFirstPendingZoneId() {
        final JpaSort sort = new JpaSort(Sort.Direction.ASC, HarvestPermitArea_.statusTime);
        final PageRequest pageRequest = new PageRequest(0, 1, sort);
        final DateTime processingSince = DateTime.now().minusHours(1);

        return harvestPermitAreaRepository
                .findInStatusPendingOrProcessingTooLong(processingSince, pageRequest)
                .stream().findFirst();
    }
}
