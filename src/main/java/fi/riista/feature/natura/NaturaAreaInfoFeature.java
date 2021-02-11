package fi.riista.feature.natura;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;

@Service
public class NaturaAreaInfoFeature {

    private static Logger LOG = LoggerFactory.getLogger(NaturaAreaInfoFeature.class);

    @Resource
    private CloseableHttpClient httpClient;

    @Value("${natura.area.info.url.template}")
    private String uriTemplate;

    public NaturaAreaInfoDTO getNaturaAreaInfo(final int zoomLevel,
                                               final int tileX,
                                               final int tileY,
                                               final int pixelX,
                                               final int pixelY) throws IOException {

        if (uriTemplate.isEmpty()) {
            LOG.warn("No Natura area info available due no URL template defined");
            return null;
        }

        final URI uri = UriComponentsBuilder.fromUriString(uriTemplate)
                .buildAndExpand(zoomLevel, tileX, tileY, pixelX, pixelY)
                .toUri();

        final HttpGet request = new HttpGet(uri);

        return httpClient.execute(request, response -> {
            final int code = response.getStatusLine().getStatusCode();

            if (code != HttpStatus.OK.value()) {
                throw new IOException(String.format("Incorrect status code when requesting Natura area info. URL:%s, code %d", uri, code));
            }

            final HttpEntity entity = response.getEntity();
            final String content = EntityUtils.toString(entity);

            if (content.length() == 0) {
                return null;
            }

            final XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(content, NaturaAreaInfoDTO.class);

        });

    }

}
