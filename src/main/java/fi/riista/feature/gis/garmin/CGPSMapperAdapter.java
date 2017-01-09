package fi.riista.feature.gis.garmin;

import com.google.common.base.Joiner;
import fi.riista.feature.huntingclub.area.HuntingClubAreaDTO;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class CGPSMapperAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CGPSMapperAdapter.class);

    private static final List<Path> BINARY_PATH_LIST = Arrays.asList(
            Paths.get("/usr/local/bin/cgpsmapper-static"),
            Paths.get("/usr/bin/cgpsmapper-static"),
            Paths.get("/data/cgpsmapper-static")
    );

    private static Optional<Path> getBinaryPath() {
        return BINARY_PATH_LIST.stream()
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable)
                .findAny();
    }

    public byte[] exportToFile(final FeatureCollection featureCollection,
                               final HuntingClubAreaDTO areaDTO) throws Exception {
        final Optional<Path> optionalBinary = getBinaryPath();

        if (!optionalBinary.isPresent()) {
            throw new IllegalStateException("Binary not found");
        }

        final Path binaryPath = optionalBinary.get();
        final File polish = File.createTempFile("garmin", ".mp");
        final File output = File.createTempFile("garmin", ".img");
        final File logFile = File.createTempFile("garmin", ".log");
        final Path polishPath = polish.toPath();
        final Path outputPath = output.toPath();

        try {
            final String polishContent = new GarminPolishFormatConverter(featureCollection, areaDTO).export();
            Files.write(polishPath, polishContent.getBytes(StandardCharsets.ISO_8859_1));

            final List<String> cmdLine = Arrays.asList(
                    binaryPath.toString(),
                    "-o",
                    outputPath.toString(),
                    polishPath.toString());

            LOG.info("cmdLine: {}", Joiner.on(' ').join(cmdLine));

            final Process process = new ProcessBuilder()
                    .command(cmdLine)
                    .redirectOutput(logFile)
                    .redirectErrorStream(true)
                    .start();

            try {
                // timeout
                if (process.waitFor(60, TimeUnit.SECONDS)) {
                    return Files.readAllBytes(outputPath);
                }
            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }

            throw new IllegalStateException("Garmin conversion has failed with exitCode=" + process.exitValue());

        } finally {
            try {
                Files.deleteIfExists(polishPath);
                Files.deleteIfExists(outputPath);
            } catch (IOException ignore) {
                LOG.error("Could not remove temporary files");
            }
        }
    }
}
