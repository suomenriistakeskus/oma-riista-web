package fi.riista.integration.lupahallinta.club;

import com.google.common.base.Stopwatch;
import fi.riista.integration.lupahallinta.support.LupahallintaHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;

import static fi.riista.integration.lupahallinta.club.LHHuntingClubBatchConfig.KEY_INPUT_FILE;

public class LHHuntingClubCSVDownloader implements Tasklet {
    private static final Logger LOG = LoggerFactory.getLogger(LHHuntingClubCSVDownloader.class);

    public static final String FILE_PREFIX = "lh-club-csv";
    public static final String FILE_SUFFIX = ".csv";

    private final LupahallintaHttpClient lupahallintaHttpClient;

    public LHHuntingClubCSVDownloader(final LupahallintaHttpClient lupahallintaHttpClient) {
        this.lupahallintaHttpClient = lupahallintaHttpClient;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution,
                                final ChunkContext chunkContext) throws Exception {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Path tempFile = Files.createTempFile(FILE_PREFIX, FILE_SUFFIX);

        try {
            final Path path = lupahallintaHttpClient.downloadClubCSV(tempFile);

            LOG.info("Download completed successfully in {}, inputFile={}", stopwatch, path);

            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put(KEY_INPUT_FILE, path.toString());

        } catch (final Exception ex) {
            Files.deleteIfExists(tempFile);
            throw ex;
        }

        return RepeatStatus.FINISHED;
    }
}
