package fi.riista.feature.permit.application.archive;

import fi.riista.api.application.HarvestPermitApplicationPdfController;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.PdfExportFactory;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.geometry.HarvestPermitApplicationAreaPartnerExportDTO;
import fi.riista.feature.permit.application.geometry.HarvestPermitApplicationGeometryFeature;
import fi.riista.feature.permit.area.mml.HarvestPermitAreaMmlPdfController;
import fi.riista.feature.permit.area.pdf.PermitAreaMapPdfFeature;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.integration.mapexport.MapPdfBasemap;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.ColorConversionUtil;
import fi.riista.util.GISUtils;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.PdfWriter;
import fi.riista.util.PolygonConversionUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private PermitAreaMapPdfFeature permitAreaMapPdfFeature;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private HarvestPermitApplicationGeometryFeature harvestPermitApplicationGeometryFeature;

    public void exportApplicationPdf(final PermitApplicationArchiveDTO dto,
                                     final Path tempFile) throws IOException {
        pdfExportFactory.create()
                .withHeaderRight(Integer.toString(dto.getApplicationNumber()))
                .withHtmlPath(HarvestPermitApplicationPdfController.getHtmlPath(dto.getId()))
                .withLanguage(dto.getLocale())
                .build()
                .export(tempFile);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportApplicationAttachments(final long applicationId,
                                             final PermitApplicationArchiveGenerator generator) throws IOException {
        final HarvestPermitApplication application = requireEntityService
                .requireHarvestPermitApplication(applicationId, EntityPermission.READ);

        for (final HarvestPermitApplicationAttachment attachment : application.getAttachments()) {
            final String originalFilename = attachment.getAttachmentMetadata().getOriginalFilename();
            final Path attachmentPath = generator.addAttachment(originalFilename);
            fileStorageService.downloadTo(attachment.getAttachmentMetadata().getId(), attachmentPath);
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
        final MapPdfModel model = permitAreaMapPdfFeature.getModelForApplication(applicationId, null, Locales.FI);

        final MapPdfParameters parameters = new MapPdfParameters();
        parameters.setLayer(MapPdfBasemap.MAASTOKARTTA);
        parameters.setPaperSize(MapPdfParameters.PaperSize.A3);
        parameters.setPaperOrientation(model.isPreferLandscape()
                ? MapPdfParameters.PaperOrientation.LANDSCAPE
                : MapPdfParameters.PaperOrientation.PORTRAIT);

        final byte[] mapPdfData = mapPdfRemoteService.renderPdf(parameters, model);

        Files.copy(new ByteArrayInputStream(mapPdfData), tempFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportMmlPdf(final PermitApplicationArchiveDTO dto, final Path tempFile) throws IOException {
        pdfExportFactory.create()
                .withHtmlPath(HarvestPermitAreaMmlPdfController.getHtmlPath(dto.getId()))
                .withLanguage(dto.getLocale())
                .build()
                .export(tempFile);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportPartnerMapPdf(final long applicationId, final Path tempFile) throws IOException {
        final HarvestPermitApplicationAreaPartnerExportDTO exportDTO = harvestPermitApplicationGeometryFeature.exportPermitAreaForEachPartner(applicationId);
        final FeatureCollection featureCollection = exportDTO.getFeatureCollection();

        for (int index = 0; index < featureCollection.getFeatures().size(); index++) {
            final Feature feature = featureCollection.getFeatures().get(index);
            final int hue = Math.round(index * 256 / (float)featureCollection.getFeatures().size()) % 256;
            feature.setProperty("fill", "hsl(" + hue + ", 100%, 40%)");
            feature.setProperty("fill-opacity", 0.5);
            feature.setProperty("stroke-width", 2.0);
            feature.setProperty("stroke", "rgb(0,0,0)");
        }

        final MapPdfModel model = new MapPdfModel.Builder(Locales.FI)
                .withFeatureCollection(featureCollection)
                .withBbox(featureCollection.getBbox())
                .build();

        final MapPdfParameters parameters = new MapPdfParameters();
        parameters.setLayer(MapPdfBasemap.TAUSTAKARTTA);
        parameters.setPaperSize(MapPdfParameters.PaperSize.A3);
        parameters.setPaperOrientation(model.isPreferLandscape()
                ? MapPdfParameters.PaperOrientation.LANDSCAPE
                : MapPdfParameters.PaperOrientation.PORTRAIT);

        final byte[] mapPdfData = mapPdfRemoteService.renderPdf(parameters, model);

        final InputStream is = new ByteArrayInputStream(mapPdfData);
        try (final PDDocument pdfDocument = PDDocument.load(is)) {
            renderPartnerMapLegend(pdfDocument, exportDTO);
            pdfDocument.save(tempFile.toFile());
        }
    }

    private static void renderPartnerMapLegend(final PDDocument pdfDocument,
                                               final HarvestPermitApplicationAreaPartnerExportDTO exportDTO) throws IOException {
        final PDPage pdfPage = new PDPage(PDRectangle.A4);
        pdfDocument.addPage(pdfPage);

        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            writer.font(PDType1Font.HELVETICA, 14);

            writer.topOffsetMm(15).marginLeftMm(15);
            writer.writeLine("Seurat ja alueet:");

            writer.topOffsetMm(25);

            final Map<Long, LocalisedString> zoneToClubName = exportDTO.getClubNames();
            final Map<Long, LocalisedString> zoneToAreaName = exportDTO.getAreaNames();

            final FeatureCollection featureCollection = exportDTO.getFeatureCollection();
            final List<Feature> features = featureCollection.getFeatures();

            for (int i = 0; i < features.size(); i++) {
                final Feature feature = features.get(i);
                final Long zoneId = Long.parseLong(feature.getId());

                final String clubName = zoneToClubName.get(zoneId).getFinnish();
                final String areaName = zoneToAreaName.get(zoneId).getFinnish();

                final int hue = Math.round(i * 256 / (float) features.size()) % 256;
                final Color color = ColorConversionUtil.hslToRgb(hue, 1, 0.4f);

                writer
                        .marginLeftMm(15)
                        .drawFilledBox(6, 4, color)
                        .marginLeftMm(25)
                        .writeLine(clubName + " - " + areaName);
            }
        }
    }
}
