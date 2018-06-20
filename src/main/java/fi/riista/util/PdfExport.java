package fi.riista.util;

import com.google.common.io.ByteStreams;
import fi.riista.security.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PdfExport {
    private static final Logger LOG = LoggerFactory.getLogger(PdfExport.class);

    private static class PdfParameters {
        private int marginTop = 10;
        private int marginBottom = 10;
        private int marginLeft = 20;
        private int marginRight = 10;
        private int dpi = 72;
        private int imageDpi = 72;
        private String headerRight = "[page] / [toPage]";
    }

    public static class Builder {
        private final UriComponentsBuilder requestBuilder;
        private final String binaryPath;
        private final PdfParameters pdfParameters = new PdfParameters();
        private String authenticationToken;

        public Builder(final UriComponentsBuilder requestBuilder, final boolean isProduction) {
            this.requestBuilder = requestBuilder;
            this.binaryPath = isProduction ? "/usr/local/bin/wkhtmltopdf" : "wkhtmltopdf";
        }

        public Builder withAuthenticationToken(final String token) {
            this.authenticationToken = token;
            return this;
        }

        public Builder withHtmlPath(final String path) {
            Objects.requireNonNull(path);
            this.requestBuilder.replacePath(path);
            return this;
        }

        public Builder withMargin(final int top, final int right, final int bottom, final int left) {
            this.pdfParameters.marginTop = top;
            this.pdfParameters.marginRight = right;
            this.pdfParameters.marginBottom = bottom;
            this.pdfParameters.marginLeft = left;
            return this;
        }

        public Builder withDpi(final int dpi) {
            this.pdfParameters.dpi = dpi;
            return this;
        }

        public Builder withImageDpi(final int imageDpi) {
            this.pdfParameters.imageDpi = imageDpi;
            return this;
        }

        public Builder withLanguage(String language) {
            if (language != null) {
                this.requestBuilder.replaceQueryParam("lang", language);
            }
            return this;
        }

        public Builder withHeaderRight(String headerRight) {
            this.pdfParameters.headerRight = headerRight + "     [page] / [toPage]";
            return this;
        }

        public PdfExport build() {
            Objects.requireNonNull(this.authenticationToken);
            return new PdfExport(requestBuilder.toUriString(), binaryPath, authenticationToken, pdfParameters);
        }
    }

    private final String requestUri;
    private final String binaryPath;
    private final String authHeaderName;
    private final String authHeaderValue;
    private final PdfParameters pdfParameters;

    private PdfExport(final String requestUri, final String binaryPath, final String authenticationToken,
                      final PdfParameters pdfParameters) {
        this.authHeaderName = JwtAuthenticationFilter.HEADER_AUTHORIZATION;
        this.authHeaderValue = JwtAuthenticationFilter.AUTHORIZATION_PREFIX + authenticationToken;
        this.requestUri = Objects.requireNonNull(requestUri);
        this.binaryPath = Objects.requireNonNull(binaryPath);
        this.pdfParameters = Objects.requireNonNull(pdfParameters);
    }

    private List<String> buildCommandLineWithArguments() {
        return Arrays.asList(this.binaryPath,
                "--custom-header", authHeaderName, authHeaderValue,
                "--dpi", Integer.toString(pdfParameters.dpi),
                "--image-quality", Integer.toString(pdfParameters.imageDpi),
                "--page-size", "A4",
                "--orientation", "Portrait",
                "--no-outline",
                "--disable-smart-shrinking",
                "--margin-top", String.format("%dmm", pdfParameters.marginTop),
                "--margin-right", String.format("%dmm", pdfParameters.marginRight),
                "--margin-bottom", String.format("%dmm", pdfParameters.marginBottom),
                "--margin-left", String.format("%dmm", pdfParameters.marginLeft),
                "--header-right", pdfParameters.headerRight,
                this.requestUri, "-");
    }

    public interface InputStreamConsumer {
        void accept(InputStream is) throws IOException;
    }

    public void export(final Path outputFile) throws IOException {
        export(inputStream -> Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING));
    }

    public void export(final OutputStream outputStream) throws IOException {
        export(inputStream -> ByteStreams.copy(inputStream, outputStream));
        outputStream.flush();
    }

    // Convert HTML page to PDF using external utility
    private void export(final InputStreamConsumer consumer) throws IOException {
        LOG.info("Using request url: {}", this.requestUri);

        final List<String> cmdLine = buildCommandLineWithArguments();
        final Process process = new ProcessBuilder(cmdLine).start();

        try {
            try {
                // Returns true if process exits within timeout
                if (process.waitFor(1, TimeUnit.SECONDS)) {
                    failOnError(process.exitValue());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try (final InputStream bis = new BufferedInputStream(process.getInputStream())) {
                consumer.accept(bis);
            }

            try {
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            failOnError(process.exitValue());

        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static void failOnError(final int exitValue) {
        if (exitValue != 0) {
            LOG.error("Process exited prematurely with failure: {}", exitValue);
            throw new IllegalStateException("PDF generation failed");
        }
    }
}
