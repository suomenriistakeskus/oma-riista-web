package fi.riista.integration.lupahallinta.club;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LHHuntingClubCSVCleaner extends JobExecutionListenerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(LHHuntingClubCSVCleaner.class);

    @Override
    public void afterJob(final JobExecution jobExecution) {
        if (ExitStatus.COMPLETED.equals(jobExecution.getExitStatus())) {
            final ExecutionContext executionContext = jobExecution.getExecutionContext();
            final String inputFile = executionContext.getString(LHHuntingClubBatchConfig.KEY_INPUT_FILE, null);

            if (inputFile != null) {
                final Path path = Paths.get(inputFile);

                try {
                    LOG.info("Deleting temporary file: {}", inputFile);
                    Files.deleteIfExists(path);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                LOG.warn("Input file not found in context");
            }
        }
    }
}
