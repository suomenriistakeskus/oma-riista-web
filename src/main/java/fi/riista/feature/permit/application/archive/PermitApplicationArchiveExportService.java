package fi.riista.feature.permit.application.archive;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.api.HarvestPermitApplicationPdfController;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.pdf.HarvestPermitApplicationMapPdfFeature;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.Locales;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

@Service
public class PermitApplicationArchiveExportService {

    @Resource
    private PdfExportFactory pdfExportFactory;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private HarvestPermitApplicationMapPdfFeature harvestPermitApplicationMapPdfFeature;

    public void exportApplicationPdf(final long applicationId, final Path tempFile) throws IOException {
        pdfExportFactory.create()
                .withHtmlPath(HarvestPermitApplicationPdfController.getHtmlPath(applicationId))
                .build()
                .export(tempFile);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportApplicationAttachments(final long applicationId,
                                             final PermitApplicationArchiveGenerator generator) throws IOException {
        final HarvestPermitApplication application = requireEntityService
                .requireHarvestPermitApplication(applicationId, EntityPermission.READ);

        for (final HarvestPermitApplicationAttachment attachment : application.getAttachments()) {
            if (attachment.getAttachmentMetadata() != null) {
                final Path attachmentPath = generator.addAttachment(attachment.getName());
                fileStorageService.downloadTo(attachment.getAttachmentMetadata().getId(), attachmentPath);
            }
        }
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportMapGeoJson(final long applicationId, final Path tempFile) throws IOException {
        final HarvestPermitApplication application = requireEntityService
                .requireHarvestPermitApplication(applicationId, EntityPermission.READ);
        application.assertHasPermitArea();

        final Long zoneId = application.getArea().getZone().getId();
        final GISUtils.SRID crs = GISUtils.SRID.ETRS_TM35FIN;
        final Geometry geometry = gisZoneRepository.getSimplifiedGeometry(zoneId, crs);
        final Feature feature = new Feature();
        feature.setBbox(GISUtils.getGeoJsonBBox(geometry));
        feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(geometry));

        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(Collections.singletonList(feature));
        featureCollection.setBbox(feature.getBbox());
        featureCollection.setCrs(crs.getGeoJsonCrs());

        objectMapper.writeValue(tempFile.toFile(), featureCollection);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportMapPdf(final long applicationId, final Path tempFile) throws IOException {
        final MapPdfModel model = harvestPermitApplicationMapPdfFeature.getModel(applicationId, Locales.FI);

        final MapPdfParameters parameters = new MapPdfParameters();
        parameters.setPaperDpi(300);
        parameters.setPaperSize(MapPdfParameters.PaperSize.A3);
        parameters.setPaperOrientation(model.isPreferLandscape()
                ? MapPdfParameters.PaperOrientation.LANDSCAPE
                : MapPdfParameters.PaperOrientation.PORTRAIT);

        final byte[] mapPdfData = harvestPermitApplicationMapPdfFeature.renderPdf(parameters, model);

        Files.copy(new ByteArrayInputStream(mapPdfData), tempFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
