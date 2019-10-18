package fi.riista.integration.common;

import fi.riista.util.ContentDispositionUtil;
import org.apache.http.HttpEntity;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class HttpProxyService {

    private static final Logger LOG = LoggerFactory.getLogger(HttpProxyService.class);

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(1500)
            .setSocketTimeout(5000)
            // When LH serves pdf, and pdf is not found for some reason, there will be redirect to error page, but error page is 200 OK.
            // Therefore do not follow redirects and treat anything else than 200 as 404 Not Found.
            .setRedirectsEnabled(false)
            .build();

    public static URI toUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid url: " + url);
        }
    }

    @Resource
    private CloseableHttpClient httpClient;

    public boolean checkHeadResponseOk(final @NotNull URI requestUri,
                                       final UsernamePasswordCredentials credentials) {
        try {
            final HttpHead request = new HttpHead(requestUri);
            request.setConfig(REQUEST_CONFIG);

            if (credentials != null) {
                request.addHeader(new BasicScheme().authenticate(credentials, request, new BasicHttpContext()));
            }

            return httpClient.execute(request, response -> response.getStatusLine().getStatusCode() == HttpStatus.OK.value());

        } catch (final Exception e) {
            LOG.error("Exception, message: " + e.getMessage());
        }

        return false;
    }

    public void downloadFile(final @NotNull HttpServletResponse httpServletResponse,
                             final @NotNull URI requestUri,
                             final UsernamePasswordCredentials credentials,
                             final String responseFileName,
                             final MediaType responseContentType) {
        try {
            final HttpGet request = new HttpGet(requestUri);
            request.setConfig(REQUEST_CONFIG);

            if (credentials != null) {
                request.addHeader(new BasicScheme().authenticate(credentials, request, new BasicHttpContext()));
            }

            httpClient.execute(request, proxyResponse -> {
                final int code = proxyResponse.getStatusLine().getStatusCode();

                if (code != HttpStatus.OK.value()) {
                    LOG.warn(String.format("Incorrect response code when downloading URL:%s code:%d", requestUri, code));
                    httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
                    return null;
                }

                final HttpEntity entity = proxyResponse.getEntity();

                httpServletResponse.setStatus(HttpStatus.OK.value());

                if (responseFileName != null) {
                    ContentDispositionUtil.addHeader(httpServletResponse, responseFileName);
                }

                if (responseContentType != null) {
                    httpServletResponse.setContentType(responseContentType.toString());
                } else if (entity.getContentType() != null) {
                    httpServletResponse.setContentType(entity.getContentType().getValue());
                }

                if (entity.getContentLength() >= 0) {
                    httpServletResponse.setContentLengthLong(entity.getContentLength());
                }

                entity.writeTo(httpServletResponse.getOutputStream());
                httpServletResponse.flushBuffer();

                return null;
            });

        } catch (final Exception e) {
            LOG.error(String.format("Exception type:%s msg:%s occurred while downloading uri:%s",
                    e.getClass().getSimpleName(), e.getMessage(), requestUri));

            httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }
}
