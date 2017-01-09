package fi.riista.integration.common;

import org.joda.time.Interval;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.item.file.FlatFileParseException;

public class LoggingBatchListener {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingBatchListener.class);

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        LOG.info("Starting job: {}", jobExecution.getJobInstance().getJobName());
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        final Interval interval = new Interval(
                jobExecution.getStartTime().getTime(),
                System.currentTimeMillis());

        LOG.info("Finished job: {} in {} with exitStatus={}",
                jobExecution.getJobInstance().getJobName(),
                PeriodFormat.getDefault().print(interval.toPeriod()),
                jobExecution.getExitStatus());
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        LOG.info("Starting step: {}", stepExecution.getStepName());
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        final Interval interval = new Interval(
                stepExecution.getStartTime().getTime(),
                System.currentTimeMillis());

        LOG.debug("Finished step: {} in {}. read = {} write = {} commit = {} rollback = {} filter = {} skip = {}",
                stepExecution.getStepName(), PeriodFormat.getDefault().print(interval.toPeriod()),
                stepExecution.getReadCount(), stepExecution.getWriteCount(),
                stepExecution.getCommitCount(), stepExecution.getRollbackCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount());

        return stepExecution.getExitStatus();
    }

/*
    @BeforeChunk
    public void afterChunk(ChunkContext context) {
        StepExecution stepExecution = context.getStepContext().getStepExecution();

        LOG.debug("Chunk completed read = {} write = {} commit = {} rollback = {} filter = {} skip = {}",
                stepExecution.getReadCount(), stepExecution.getWriteCount(),
                stepExecution.getCommitCount(), stepExecution.getRollbackCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount());
    }
*/

    @OnReadError
    public void onReadError(Exception ex) {
        if (ex instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) ex;
            LOG.error("Error reading data on line '{}' - data: '{}'", ffpe.getLineNumber(), ffpe.getInput());
        }
    }

    @OnSkipInRead
    public void onSkipInRead(Throwable throwable) {
        // Casting Throwable to Object to avoid the version of error() that takes a String and Throwable as parameters.
        // This way the stack trace won't be printed.
        LOG.error("Skipped import file line in read because of exception: {}", (Object) throwable);
    }

    @OnSkipInWrite
    public void onSkipInWrite(Object item, Throwable throwable) {
        LOG.error("Skipped item while writing: {} because of exception {}", item, throwable.getMessage());
    }

    @OnSkipInProcess
    public void onSkipInProcess(Object item, Throwable throwable) {
        LOG.error("Skipped item while processing: {} because of exception {}", item, throwable.getMessage());
    }
}
