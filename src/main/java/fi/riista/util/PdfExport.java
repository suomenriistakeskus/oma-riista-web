package fi.riista.util;

import com.google.common.io.ByteStreams;
import fi.riista.security.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PdfExport {
    private static final Logger LOG = LoggerFactory.getLogger(PdfExport.class);

    private final UriComponentsBuilder requestBuilder;
    private final String jwtToken;
    private String fileName;
    private final boolean isProduction;

    public PdfExport(final HttpServletRequest httpServletRequest,
                     final String jwtToken,
                     boolean isProduction) {
        this.jwtToken = Objects.requireNonNull(jwtToken);
        this.requestBuilder = ServletUriComponentsBuilder.fromRequestUri(httpServletRequest);
        this.fileName = "" + System.currentTimeMillis() + ".pdf";
        this.isProduction = isProduction;
    }

    public PdfExport withFileName(final String name) {
        Objects.requireNonNull(name);
        this.fileName = name;
        return this;
    }

    public PdfExport withHtmlPath(final String path) {
        Objects.requireNonNull(path);
        this.requestBuilder.replacePath(path);
        return this;
    }

    public PdfExport withLanguage(String language) {
        if (language != null) {
            this.requestBuilder.replaceQueryParam("lang", language);
        }
        return this;
    }

    // Convert HTML page to PDF using external utility
    public void export(final HttpServletRequest request,
                       final HttpServletResponse response) throws IOException {
        final String pdfUrl = this.requestBuilder.toUriString();

        LOG.info("Using request url: {}", pdfUrl);

        final List<String> cmdLine = buildCommandLineWithArguments(isProduction, jwtToken, pdfUrl);
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

            response.addHeader(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(fileName));

            try (final InputStream is = new BufferedInputStream(process.getInputStream());
                 final OutputStream os = response.getOutputStream()) {
                ByteStreams.copy(is, response.getOutputStream());
                os.flush();
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

    private static List<String> buildCommandLineWithArguments(final boolean isProduction,
                                                       final String jwtToken,
                                                       final String pdfUrl) {
        final String bin = isProduction ? "/usr/local/bin/wkhtmltopdf" : "wkhtmltopdf";

        final String authHeaderName = JwtAuthenticationFilter.HEADER_AUTHORIZATION;
        final String authHeaderValue = JwtAuthenticationFilter.AUTHORIZATION_PREFIX + jwtToken;

        return Arrays.asList(
                bin,
                "--custom-header", authHeaderName, authHeaderValue,
                "--header-right", "[page] / ( [toPage] )    ",
                "--image-quality", "96",
                "-B", "0", "-R", "0", "-T", "0", "-L", "0",
                pdfUrl, "-");
    }

    private static void failOnError(final int exitValue) {
        if (exitValue != 0) {
            LOG.error("Process exited prematurely with failure: {}", exitValue);
            throw new IllegalStateException("PDF generation failed");
        }
    }
}
