package fi.riista.integration.metsastajarekisteri.innofactor;

import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriJobParameters;
import fi.riista.integration.metsastajarekisteri.input.PendingImportFile;
import fi.riista.integration.metsastajarekisteri.input.PendingImportFileFilter;
import fi.riista.util.DateUtil;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

@Component
public class InnofactorImportRunner {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyyMMddHHmm");

    private static final Logger LOG = LoggerFactory.getLogger(InnofactorImportRunner.class);

    @Resource
    private JobLauncher jobLauncher;

    @Resource(name = InnofactorImportConfig.JOB_NAME)
    private Job job;

    private Path temporaryPath;

    @PostConstruct
    public void init() throws IOException {
        this.temporaryPath = Files.createTempDirectory("metsastajarekisteri").toAbsolutePath();
    }

    public Set<PendingImportFile> scanForPendingUploads(Path path) {
        final PendingImportFileFilter fileCollector = PendingImportFileFilter.createDefault();

        return fileCollector.processPath(path);
    }

    public PendingImportFile prepareUpload(final MultipartFile multiPart) throws IOException {
        final String fileName = "metsastajarekisteri-" + DATE_TIME_FORMAT.print(DateUtil.now()) + ".csv.gz.enc";
        final String markerFileName = fileName + ".complete";

        final Path filePath = temporaryPath.resolve(fileName).normalize().toAbsolutePath();
        final Path markerFilePath = temporaryPath.resolve(markerFileName).normalize().toAbsolutePath();

        LOG.info("Storing import dump file as: {}", filePath);

        Files.copy(multiPart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        Files.createFile(markerFilePath);

        final BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);

        return new PendingImportFile(filePath, fileAttributes, markerFilePath);
    }

    @Async
    public void runAsync(final PendingImportFile entry) throws Exception {
        run(entry);
    }

    public void run(final PendingImportFile entry) throws Exception {
        entry.removeMarkerFile();

        final JobExecution jobExecution = jobLauncher.run(job, MetsastajaRekisteriJobParameters.createJobParameters(entry));

        LOG.info("Job execution scheduled with id={}", jobExecution.getId());
    }
}
