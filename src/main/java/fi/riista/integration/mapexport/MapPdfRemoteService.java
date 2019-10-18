package fi.riista.integration.mapexport;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import io.sentry.Sentry;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.function.Supplier;

@Service
public class MapPdfRemoteService {
    private static final Logger LOG = LoggerFactory.getLogger(MapPdfRemoteService.class);

    private static final int PAPER_DPI = 300;

    // Change this constants to adjust source material zoome level.
    // If this value is too big, then text in raster can be too small to read.
    private static final int MAP_SOURCE_DPI = 170;

    private static final double PAPER_TOP_BOTTOM_MARGIN = 0.045; // metres
    private static final double PAPER_LEFT_RIGHT_MARGIN = 0.02; // metres

    private static final double INCH_IN_METERS = 0.0254;

    private static String getRemoteUri(final URI baseUri, final MapPdfParameters parameters, final int zoomLayer) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path(String.format("/%s%s/%d/%s%d.pdf",
                        parameters.getPaperSize().name(),
                        parameters.getPaperOrientation().asLetter(),
                        PAPER_DPI,
                        parameters.getLayer().getName(),
                        zoomLayer))
                .build()
                .toUri()
                .toASCIIString();
    }

    private static int zoomLayer(double[] bbox, final MapPdfParameters parameters) {
        if (bbox == null || bbox.length != 4) {
            throw new IllegalArgumentException("Area bounding box is invalid");
        }

        final double areaWidth = Math.abs(bbox[2] - bbox[0]);
        final double areaHeight = Math.abs(bbox[3] - bbox[1]);

        // Hard-coded map margins from mapexport service

        final MapPdfParameters.PaperSize paperSize = parameters.getPaperSize();
        final double paperWidth = paperSize.getWidth(parameters.getPaperOrientation());
        final double paperHeight = paperSize.getHeight(parameters.getPaperOrientation());

        final double mapWidthPx = (paperWidth - PAPER_LEFT_RIGHT_MARGIN) / INCH_IN_METERS * MAP_SOURCE_DPI;
        final double mapHeightPx = (paperHeight - PAPER_TOP_BOTTOM_MARGIN) / INCH_IN_METERS * MAP_SOURCE_DPI;
        final double resolution = Math.max(areaWidth / mapWidthPx, areaHeight / mapHeightPx);

        // For ETRS-TM35FIN following applies
        // res = metre / pixel = pow(2, 13 - zoom)
        // zoom = 13 - ln2(res)
        final double zoom = Math.floor(13 - (Math.log(resolution) / Math.log(2)));

        if (zoom > 15) {
            return 15;
        } else if (zoom < 0) {
            return 0;
        } else {
            return (int) zoom;
        }
    }

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Autowired
    public MapPdfRemoteService(final ClientHttpRequestFactory requestFactory) {
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public ResponseEntity<?> renderPdf(final MapPdfParameters parameters,
                                       final Supplier<MapPdfModel> modelProvider) {
        try {
            final MapPdfModel model = modelProvider.get();
            final byte[] pdfData = renderPdf(parameters, model);

            return ResponseEntity.ok()
                    .contentType(MediaTypeExtras.APPLICATION_PDF)
                    .contentLength(pdfData.length)
                    .headers(ContentDispositionUtil.header(model.getExportFileName()))
                    .body(pdfData);

        } catch (final Exception ex) {
            LOG.error("Area printing has failed with " + parameters.toString(), ex);

            Sentry.capture(ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaTypeExtras.TEXT_PLAIN_UTF8)
                    .body("Kartan tulostus epäonnistui. Yritä myöhemmin uudelleen");
        }
    }

    public byte[] renderPdf(final MapPdfParameters parameters, final MapPdfModel model) {
        final int zoomLayer = zoomLayer(model.getFeatures().getBbox(), parameters);
        final String url = getRemoteUri(runtimeEnvironmentUtil.getMapExportEndpoint(), parameters, zoomLayer);
        return callRemoteService(url, model.getFeatures());
    }

    private byte[] callRemoteService(final String url, final FeatureCollection features) {
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaTypeExtras.APPLICATION_PDF));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Accept-Encoding", "gzip");

        final HttpEntity<FeatureCollection> requestEntity = new HttpEntity<>(features, requestHeaders);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class).getBody();
    }
}
