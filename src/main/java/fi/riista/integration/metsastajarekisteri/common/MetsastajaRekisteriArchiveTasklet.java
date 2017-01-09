package fi.riista.integration.metsastajarekisteri.common;

import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class MetsastajaRekisteriArchiveTasklet implements Tasklet {
    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriArchiveTasklet.class);

    private final FileStorageService fileStorageService;

    public MetsastajaRekisteriArchiveTasklet(final FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution,
                                final ChunkContext chunkContext) throws Exception {
        final MetsastajaRekisteriJobParameters jobParameters = new MetsastajaRekisteriJobParameters(
                chunkContext.getStepContext().getJobParameters());

        final Path sourcePath = Paths.get(jobParameters.getInputFile());
        final File sourceFile = sourcePath.toFile();

        final UUID uuid = UUID.randomUUID();

        LOG.info("Archiving file with UUID={} filename={}", uuid, sourceFile.getName());

        fileStorageService.storeFile(
                uuid,
                sourceFile,
                FileType.METSASTAJAREKISTERI,
                "application/octet-stream",
                sourceFile.getName());

        Files.delete(sourcePath);

        return RepeatStatus.FINISHED;
    }
}

