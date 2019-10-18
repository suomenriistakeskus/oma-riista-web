package fi.riista.integration.garmin;

import com.google.common.base.Joiner;
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

    public byte[] exportToFile(final FeatureCollection featureCollection, final long areaId, final String areaName) {
        final Optional<Path> optionalBinary = getBinaryPath();

        if (!optionalBinary.isPresent()) {
            throw new IllegalStateException("Binary not found");
        }

        try {
            final File tempDir = com.google.common.io.Files.createTempDir();
            final Path polishPath = new File(tempDir, "input.mp").toPath();
            final Path outputPath = new File(tempDir, "output.img").toPath();
            final File logFile = new File(tempDir, "output.log");
            final Path binaryPath = optionalBinary.get();

            final String polishContent = new GarminPolishFormatConverter(featureCollection, areaId, areaName).export();
            Files.write(polishPath, polishContent.getBytes(StandardCharsets.ISO_8859_1));

            final List<String> cmdLine = Arrays.asList(
                    binaryPath.toString(),
                    "-o",
                    outputPath.getFileName().toString(),
                    polishPath.getFileName().toString());

            LOG.info("cmdLine: {}", Joiner.on(' ').join(cmdLine));

            final Process process = new ProcessBuilder(cmdLine)
                    // Segfaults unless running in same directory as input and output files
                    .directory(tempDir)
                    .redirectOutput(logFile)
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                // timeout
                process.destroyForcibly();
                throw new IllegalStateException("Process timeout");
            }

            if (process.exitValue() != 0) {
                throw new IllegalStateException("Conversion has failed with exitCode " + process.exitValue());
            }

            final byte[] resultBytes = Files.readAllBytes(outputPath);

            if (resultBytes.length == 0) {
                throw new IllegalStateException("Output file is empty");
            }

            try {
                // Delete temporary files only if conversion is successful
                Files.deleteIfExists(polishPath);
                Files.deleteIfExists(outputPath);
            } catch (IOException ignore) {
                LOG.error("Could not remove temporary files");
            }

            return resultBytes;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
