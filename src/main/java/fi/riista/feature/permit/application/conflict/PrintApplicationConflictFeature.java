package fi.riista.feature.permit.application.conflict;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.kiinteisto.GISPropertyGeometryRepository;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.PdfWriter;
import fi.riista.util.PolygonConversionUtil;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class PrintApplicationConflictFeature {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private static final int MAX_PROPERTY_PER_SHEET = 34;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private GISPropertyGeometryRepository propertyGeometryRepository;

    public byte[] createPdf(final PrintApplicationConflictMapModel model,
                            final MapPdfParameters mapParameters) throws IOException {
        final byte[] pdfData = mapPdfRemoteService.renderPdf(mapParameters, model.getUnion());
        final List<Path> componentPdfPaths = new LinkedList<>();

        for (final MapPdfModel componentMapModel : model.getComponents()) {
            final Path path = Files.createTempFile("component", ".pdf");
            componentPdfPaths.add(path);

            final byte[] data = mapPdfRemoteService.renderPdf(mapParameters, componentMapModel);
            Files.write(path, data);
        }

        final Path unionPdfPath = Files.createTempFile("union", ".pdf");

        try (final InputStream is = new ByteArrayInputStream(pdfData);
             final PDDocument pdfDocument = PDDocument.load(is)) {

            for (final List<PrintApplicationConflictMapModel.PropertyInfo> partition : Lists.partition(model.getPropertyList(), MAX_PROPERTY_PER_SHEET)) {
                renderPageOfProperties(pdfDocument, partition);
            }

            pdfDocument.save(unionPdfPath.toFile());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mergeOutput(componentPdfPaths, unionPdfPath);
    }

    private byte[] mergeOutput(final List<Path> componentPdfPaths, final Path unionPdfPath) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationStream(bos);
        pdfMerger.addSource(unionPdfPath.toFile());

        for (final Path componentPdfPath : componentPdfPaths) {
            pdfMerger.addSource(componentPdfPath.toFile());
        }

        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        Files.deleteIfExists(unionPdfPath);

        for (Path componentPdfPath : componentPdfPaths) {
            Files.deleteIfExists(componentPdfPath);
        }

        return bos.toByteArray();
    }

    private void renderPageOfProperties(final PDDocument pdfDocument,
                                        final List<PrintApplicationConflictMapModel.PropertyInfo> propertyList) throws IOException {
        final PDPage pdfPage = new PDPage(PDRectangle.A4);
        pdfDocument.addPage(pdfPage);

        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            writer.font(PDType1Font.HELVETICA, 14);

            writer.topOffsetMm(15).marginLeftMm(15);
            writer.writeLine("Konfliktialueen sisältämät kiinteistöt:");

            writer.topOffsetMm(25).marginLeftMm(15);

            for (final PrintApplicationConflictMapModel.PropertyInfo property : propertyList) {
                writer.writeLine(property.getPropertyNumber());
            }

            writer.topOffsetMm(25).marginLeftMm(75);

            for (final PrintApplicationConflictMapModel.PropertyInfo property : propertyList) {
                if (StringUtils.hasText(property.getPropertyName())) {
                    writer.writeLine(property.getPropertyName());
                } else {
                    writer.writeEmptyLine();
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public PrintApplicationConflictMapModel getModel(final PrintApplicationConflictRequestDTO dto,
                                                     final Locale locale) {
        final FeatureCollection features = propertyGeometryRepository.findAll(dto.getPalstaIds(), GISUtils.SRID.ETRS_TM35FIN);

        final MapPdfModel unionModel = createUnionMapModel(locale, features);

        final List<MapPdfModel> componentMapModels = features.getFeatures().stream()
                .map(f -> createComponentMapModel(locale, f)).collect(toList());

        final List<PrintApplicationConflictMapModel.PropertyInfo> propertyNumberList = features.getFeatures().stream()
                .map(f -> {
                    final String propertyNumber = f.getProperty(GeoJSONConstants.PROPERTY_NUMBER);
                    final String propertyNumberDelimited = PropertyIdentifier.create(propertyNumber).getDelimitedValue();
                    final String propertyName = f.getProperty(GeoJSONConstants.PROPERTY_NAME);

                    return new PrintApplicationConflictMapModel.PropertyInfo(propertyNumberDelimited, propertyName);
                })
                .distinct()
                .sorted(comparing(PrintApplicationConflictMapModel.PropertyInfo::getPropertyNumber))
                .collect(toList());

        return new PrintApplicationConflictMapModel(unionModel, componentMapModels, propertyNumberList);
    }

    private MapPdfModel createComponentMapModel(final Locale locale, final Feature f) {
        final Geometry geometry = PolygonConversionUtil.geoJsonToJava(f.getGeometry(), GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds bounds = GISBounds.create(geometry.getEnvelopeInternal());
        final String propertyNumber = f.getProperty(GeoJSONConstants.PROPERTY_NUMBER);
        final String propertyNumberDelimited = PropertyIdentifier.create(propertyNumber).getDelimitedValue();
        final String propertyName = f.getProperty(GeoJSONConstants.PROPERTY_NAME);
        final String propertyId = f.getId();

        return new MapPdfModel.Builder(locale)
                .withClubName(LocalisedString.of(propertyNumberDelimited + " - " + propertyId))
                .withAreaName(LocalisedString.of(propertyName))
                .withBbox(bounds.toBBox())
                .withGeometry(geometry)
                .build();
    }

    private MapPdfModel createUnionMapModel(final Locale locale, final FeatureCollection features) {
        final List<Geometry> geometryList = features.getFeatures().stream()
                .map(f -> PolygonConversionUtil.geoJsonToJava(f.getGeometry(), GISUtils.SRID.ETRS_TM35FIN))
                .collect(toList());

        final Geometry unionGeometry = GISUtils.computeUnionFaster(geometryList);
        final GISBounds unionBounds = GISBounds.create(unionGeometry.getEnvelopeInternal());

        return new MapPdfModel.Builder(locale)
                .withClubName(LocalisedString.of("Konfliktialueen kartta"))
                .withAreaName(LocalisedString.of("Tulostettu " + DATE_FORMAT.print(DateUtil.now())))
                .withBbox(unionBounds.toBBox())
                .withGeometry(unionGeometry)
                .build();
    }
}
