package fi.riista.feature.permit.application.metsahallitus;

import fi.riista.config.HttpClientConfig;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.error.NotFoundException;
import fi.riista.util.ContentDispositionUtil;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

@Component
public class MetsahallitusAreaPermitImportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MetsahallitusAreaPermitImportFeature.class);

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(HttpClientConfig.CONNECT_TIMEOUT)
            .setSocketTimeout(HttpClientConfig.READ_TIMEOUT)
            // Lets assume, that server will not work properly, and will be redirect to error page, but error page is 200 OK.
            // Therefore, do not follow redirects, and treat all other than 200 as error.
            .setRedirectsEnabled(false)
            .build();

    @Resource
    private CloseableHttpClient httpClient;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Value("${mh.import.permitarea.user}")
    private String username;

    @Value("${mh.import.permitarea.pass}")
    private String password;

    @Value("${mh.import.permitarea.url.prefix}")
    private URI baseUri;

    public MetsahallitusAreaPermitUrlsDTO fetchUrls(final MetsahallitusAreaPermitNumbersDTO dto) throws AuthenticationException, IOException {
        final URI requestUri = buildUri(dto);
        final HttpGet request = makeRequest(requestUri);

        return httpClient.execute(request, response -> {
            final int code = response.getStatusLine().getStatusCode();

            if (code != HttpStatus.OK.value()) {
                LOG.warn(String.format("Incorrect response code when downloading URL:%s code:%d", requestUri, code));
                throw new NotFoundException();
            }

            final HttpEntity entity = response.getEntity();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            entity.writeTo(bos);

            return objectMapper.readValue(bos.toByteArray(), MetsahallitusAreaPermitUrlsDTO.class);
        });
    }

    private HttpGet makeRequest(final URI requestUri) throws AuthenticationException {
        final HttpGet request = new HttpGet(requestUri);
        request.setConfig(REQUEST_CONFIG);
        request.addHeader(new BasicScheme().authenticate(credentials(), request, new BasicHttpContext()));
        return request;
    }

    private UsernamePasswordCredentials credentials() {
        return new UsernamePasswordCredentials(username, password);
    }

    private URI buildUri(final MetsahallitusAreaPermitNumbersDTO dto) {
        return UriComponentsBuilder.fromUri(baseUri)
                .replaceQueryParam("application", dto.getMhApplicationNumber())
                .replaceQueryParam("verdict", dto.getMhPermitNumber())
                .build()
                .toUri();
    }

    public MultipartFile downloadFile(final String url, final String fileNamePrefix) throws AuthenticationException, IOException {
        final URI requestUri = UriComponentsBuilder.fromHttpUrl(url).build().toUri();
        final HttpGet request = makeRequest(requestUri);
        return httpClient.execute(request, response -> {
            final int code = response.getStatusLine().getStatusCode();
            if (code != HttpStatus.OK.value()) {
                LOG.warn(String.format("Incorrect response code when downloading URL:%s code:%d", requestUri, code));
                throw new NotFoundException();
            }
            final HttpEntity entity = response.getEntity();
            final String contentType = entity.getContentType().getValue();
            final String fileName = ContentDispositionUtil.decodeAttachmentFileName(response.getHeaders("Content-Disposition")[0].getValue());
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            entity.writeTo(bos);
            byte[] bytes = bos.toByteArray();
            return createMultipartFile(fileNamePrefix, contentType, fileName, bytes);
        });
    }

    private static MultipartFile createMultipartFile(@Nonnull final String fileNamePrefix,
                                                     @Nonnull final String contentType,
                                                     @Nonnull final String fileName,
                                                     @Nonnull final byte[] bytes) {
        Objects.requireNonNull(fileNamePrefix);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(bytes);

        return new MultipartFile() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getOriginalFilename() {
                return fileNamePrefix + "_" + fileName;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return bytes.length > 0;
            }

            @Override
            public long getSize() {
                return bytes.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return bytes;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public void transferTo(final File dest) throws IOException, IllegalStateException {
                throw new RuntimeException();
            }
        };
    }
}
