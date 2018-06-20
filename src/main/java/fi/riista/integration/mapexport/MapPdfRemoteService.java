package fi.riista.integration.mapexport;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.util.MediaTypeExtras;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.Collections;

@Service
public class MapPdfRemoteService {
    private static final String LAYER_NAME = "maasto";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Autowired
    public MapPdfRemoteService(final ClientHttpRequestFactory requestFactory) {
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public byte[] renderPdf(final MapPdfParameters parameters, final FeatureCollection featureCollection) {
        final long zoomLayer = zoomLayer(featureCollection.getBbox(), parameters.getPaperDpi());

        final String url = UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getMapExportEndpoint())
                .path(String.format("/%s%s/%d/%s%d.pdf",
                        parameters.getPaperSize().name(),
                        parameters.getPaperOrientation().asLetter(),
                        parameters.getPaperDpi(),
                        LAYER_NAME,
                        zoomLayer))
                .build()
                .toUri()
                .toASCIIString();

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaTypeExtras.APPLICATION_PDF));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Accept-Encoding", "gzip");

        final HttpEntity<FeatureCollection> requestEntity = new HttpEntity<>(featureCollection, requestHeaders);

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class).getBody();
    }

    private static long zoomLayer(double[] bbox, final int dpi) {
        if (bbox == null || bbox.length != 4) {
            throw new IllegalArgumentException("Area bounding box is invalid");
        }

        final double areaWidth = Math.abs(bbox[2] - bbox[0]);
        final double areaHeight = Math.abs(bbox[3] - bbox[1]);
        final double maxDimension = Math.max(areaWidth, areaHeight);
        final double paperMaxDimension = 0.21; // metres
        final double inchInMeters = 0.0254;
        final double paperPixels = paperMaxDimension / inchInMeters * dpi;
        final double resolution = maxDimension / paperPixels;

        // For ETRS-TM35FIN following applies
        // res = metre / pixel = pow(2, 13 - zoom)
        // zoom = 13 - ln2(res)
        final double zoom = Math.floor(13 - (Math.log(resolution) / Math.log(2)));

        if (zoom > 13) {
            return 13;
        } else if (zoom < 0) {
            return 0;
        } else {
            return Math.round(zoom);
        }
    }
}
