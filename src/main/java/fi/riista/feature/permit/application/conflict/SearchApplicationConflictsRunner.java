package fi.riista.feature.permit.application.conflict;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SearchApplicationConflictsRunner {
    private static final Logger LOG = LoggerFactory.getLogger(SearchApplicationConflictsRunner.class);

    @Value("${db.max.concurrency}")
    private int maximumDatabaseConcurrency;

    @Resource
    private SearchApplicationConflictsFeature processHarvestPermitApplicationFeature;

    private ThreadPoolExecutor executorService;
    private LinkedBlockingQueue<Runnable> workQueue;

    @PostConstruct
    public void initExecutor() {
        this.workQueue = new LinkedBlockingQueue<>();
        this.executorService = new ThreadPoolExecutor(maximumDatabaseConcurrency, maximumDatabaseConcurrency,
                0L, TimeUnit.MILLISECONDS, this.workQueue);
    }

    @PreDestroy
    public void shutdownExecutor() {
        workQueue.clear();
        executorService.shutdownNow();
    }

    public void run() {
        if (processHarvestPermitApplicationFeature.isConflictCalculationPending()) {
            calculateConflictingApplications();
        }
        calculatePalstaConflicts();
    }

    private void calculateConflictingApplications() {
        LOG.info("Starting ...");

        final List<Long> applicationIds = processHarvestPermitApplicationFeature.getApplicationIdsToCalculate();
        if (applicationIds.isEmpty()) {
            return;
        }
        final int taskCount = applicationIds.size();
        final CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        final AtomicInteger successCounter = new AtomicInteger(0);
        final AtomicInteger failureCounter = new AtomicInteger(0);
        for (final long applicationId : applicationIds) {
            executorService.submit(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();

                try {
                    LOG.info("Applications remaining {} / {}. Successful={} failed={}",
                            countDownLatch.getCount(), taskCount, successCounter, failureCounter);

                    processHarvestPermitApplicationFeature.calculateConflictingApplications(applicationId);
                    successCounter.incrementAndGet();

                } catch (Exception e) {
                    LOG.error("Conflict processing failed for applicationId=" + applicationId, e);
                    failureCounter.incrementAndGet();
                } finally {
                    countDownLatch.countDown();
                    LOG.info("Processing applicationId={} took {}", applicationId, stopwatch);
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            workQueue.clear();
        }
        LOG.info("Done.");
    }

    private void calculatePalstaConflicts() {
        final List<Long> conflictIdList = processHarvestPermitApplicationFeature.getConflictIdListForSecondStep();

        if (conflictIdList.isEmpty()) {
            return;
        }

        final int taskCount = conflictIdList.size();
        final CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        final AtomicInteger successCounter = new AtomicInteger(0);
        final AtomicInteger failureCounter = new AtomicInteger(0);

        for (final Long conflictId : conflictIdList) {
            executorService.submit(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();

                try {
                    LOG.info("Conflicts remaining {} / {}. Successful={} failed={}",
                            countDownLatch.getCount(), taskCount, successCounter, failureCounter);

                    processHarvestPermitApplicationFeature.calculatePalstaListForConflict(conflictId);
                    successCounter.incrementAndGet();

                } catch (Exception e) {
                    LOG.error("Conflict processing failed for conflictId=" + conflictId, e);
                    failureCounter.incrementAndGet();
                } finally {
                    countDownLatch.countDown();
                    LOG.info("Processing conflictId={} took {}", conflictId, stopwatch);
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            workQueue.clear();
        }
    }
}
