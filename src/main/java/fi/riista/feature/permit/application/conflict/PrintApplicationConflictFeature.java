package fi.riista.feature.permit.application.conflict;

import com.google.common.collect.Lists;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.kiinteisto.GISPropertyGeometryRepository;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.PrintApplicationApproachMapFeatureCollection;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
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
import org.locationtech.jts.geom.Geometry;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class PrintApplicationConflictFeature {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private static final int MAX_PROPERTY_PER_SHEET = 34;

    private static final String LOCALIZATION_KEY_PREFIX = "PrintApplicationConflictFeature.";

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private GISPropertyGeometryRepository propertyGeometryRepository;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitApplicationConflictPalstaRepository harvestPermitApplicationConflictPalstaRepository;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public byte[] createPdf(final PrintApplicationConflictMapModel model, final Locale locale) throws IOException {
        final byte[] pdfData = mapPdfRemoteService.renderConflictPdf(model.getUnion());
        final List<Path> componentPdfPaths = new LinkedList<>();

        for (final PrintApplicationApproachMapFeatureCollection componentMapFeatures : model.getComponents()) {
            final Path path = Files.createTempFile("component", ".pdf");
            componentPdfPaths.add(path);

            final byte[] data = mapPdfRemoteService.renderConflictPdf(componentMapFeatures);
            Files.write(path, data);
        }

        final Path unionPdfPath = Files.createTempFile("union", ".pdf");

        try (final InputStream is = new ByteArrayInputStream(pdfData);
             final PDDocument pdfDocument = PDDocument.load(is)) {

            renderPageOfApplicants(pdfDocument, model.getFirstApplicant(), model.getSecondApplicant());

            for (final List<PrintApplicationConflictMapModel.PropertyInfo> partition : Lists.partition(model.getPropertyList(), MAX_PROPERTY_PER_SHEET)) {
                renderPageOfProperties(pdfDocument, partition, locale);
            }

            pdfDocument.save(unionPdfPath.toFile());

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return mergeOutput(componentPdfPaths, unionPdfPath);
    }

    private static byte[] mergeOutput(final List<Path> componentPdfPaths,
                                      final Path unionPdfPath) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationStream(bos);
        pdfMerger.addSource(unionPdfPath.toFile());

        for (final Path componentPdfPath : componentPdfPaths) {
            pdfMerger.addSource(componentPdfPath.toFile());
        }

        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        Files.deleteIfExists(unionPdfPath);

        for (final Path componentPdfPath : componentPdfPaths) {
            Files.deleteIfExists(componentPdfPath);
        }

        return bos.toByteArray();
    }

    private void renderPageOfProperties(final PDDocument pdfDocument,
                                               final List<PrintApplicationConflictMapModel.PropertyInfo> propertyList,
                                               final Locale locale) throws IOException {
        PDPage pdfPage = new PDPage(PDRectangle.A4);
        pdfDocument.addPage(pdfPage);
        final NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        final Iterator<PrintApplicationConflictMapModel.PropertyInfo> propertyIterator = propertyList.iterator();

        while (propertyIterator.hasNext()) {
            try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {

                writer.topOffsetMm(15).marginLeftMm(15);
                writer.writeLine(i18n(locale, "title"));

                writer.topOffsetMm(25);
                writer.marginLeftMm(65).write(i18n(locale, "totalArea"))
                        .marginLeftMm(95).write(i18n(locale, "landArea"))
                        .marginLeftMm(145).write(i18n(locale, "waterArea"))
                        .writeEmptyLine().writeEmptyLine();

                while (propertyIterator.hasNext()) {
                    final PrintApplicationConflictMapModel.PropertyInfo property = propertyIterator.next();

                    writer.marginLeftMm(15);
                    writer.write(property.getPropertyNumber());

                    writer.marginLeftMm(65);
                    final String totalArea = nf.format(property.getPropertyAreaSize() / 10_000);
                    writer.write(totalArea + " "+ i18n(locale, "hectares"));

                    writer.marginLeftMm(95);
                    final String landArea = nf.format(property.getPropertyLandAreaSize() / 10_000);
                    final String conflictLandArea = nf.format(property.getPropertyConflictLandAreaSize() / 10_000);
                    writer.write(landArea + " / " + conflictLandArea + " "+ i18n(locale, "hectares"));

                    writer.marginLeftMm(145);
                    final String waterArea = nf.format(property.getPropertyWaterAreaSize() / 10_000);
                    final String conflictWaterArea = nf.format(property.getPropertyConflictWaterAreaSize() / 10_000);
                    writer.writeLine(waterArea + " / " + conflictWaterArea + " "+ i18n(locale, "hectares"));

                    if (StringUtils.hasText(property.getPropertyName())) {
                        writer.marginLeftMm(25);
                        writer.italicFont().writeParagraph(property.getPropertyName(), 95f);
                    }

                    writer.normalFont().writeEmptyLine();

                    final float sizeNeededForContent = 3 * writer.getLineHeight();
                    final float marginBottom = writer.getMarginFromMm(15);
                    final float currentPos = writer.getPosY();
                    if ((currentPos - marginBottom - sizeNeededForContent) < 0) {
                        pdfPage = new PDPage(PDRectangle.A4);
                        pdfDocument.addPage(pdfPage);
                        break;
                    }

                }
            }
        }
    }

    private static void renderPageOfApplicants(final PDDocument pdfDocument,
                                               final String firstApplicant,
                                               final String secondApplicant) throws IOException {
        final PDPage pdfPage = new PDPage(PDRectangle.A4);
        pdfDocument.addPage(pdfPage);

        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            writer.font(PDType1Font.HELVETICA, 14);

            writer.topOffsetMm(15).marginLeftMm(15);
            writer.writeLine("Luvansaajien nimet:");

            writer.topOffsetMm(25).marginLeftMm(15);

            writer.writeLine(firstApplicant);
            writer.writeLine(secondApplicant);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public PrintApplicationConflictMapModel getModel(final PrintApplicationConflictRequestDTO dto,
                                                     final Locale locale) {
        final HarvestPermitApplication firstApplication =
                requireEntityService.requireHarvestPermitApplication(dto.getFirstApplicationId(), EntityPermission.READ);
        final HarvestPermitApplication secondApplication =
                requireEntityService.requireHarvestPermitApplication(dto.getSecondApplicationId(), EntityPermission.READ);

        final HarvestPermitArea firstApplicationArea = firstApplication.getArea();
        final HarvestPermitArea secondApplicationArea = secondApplication.getArea();

        final FeatureCollection features = propertyGeometryRepository.findAll(dto.getPalstaIds(),
                GISUtils.SRID.ETRS_TM35FIN);

        final FeatureCollection unionMap = createUnionMapFeatures(locale, features);
        final FeatureCollection unionApproachMap =
                createApplicationAreaMapFeatures(firstApplicationArea, secondApplicationArea, unionMap);

        final MapPdfParameters parameters = dto.getMapParameters();
        final String baseMap = parameters.getLayer().getName();
        final String paperSize = parameters.getPaperSize().name();
        final String orientation = parameters.getPaperOrientation().asLetter();

        final int unionMapZoom = MapPdfRemoteService.zoomLayer(unionMap.getBbox(), parameters);
        final int unionApproachMapZoom = MapPdfRemoteService.zoomLayer(unionApproachMap.getBbox(), parameters);
        final PrintApplicationApproachMapFeatureCollection unionFeatures =
                new PrintApplicationApproachMapFeatureCollection(
                        unionMap,
                        baseMap + unionMapZoom,
                        unionApproachMap,
                        baseMap + unionApproachMapZoom,
                        paperSize,
                        orientation);

        final List<Integer> intIds = dto.getPalstaIds().stream()
                .mapToInt(Long::intValue)
                .boxed()
                .collect(Collectors.toList());

        final Map<Long, PalstaConflictSummaryDTO> conflictSummaries = harvestPermitApplicationConflictPalstaRepository.getPalstaConflictSummaries(intIds, firstApplication, secondApplication);

        final List<PrintApplicationApproachMapFeatureCollection> componentFeatures = features.getFeatures().stream()
                .map(f -> {
                    final FeatureCollection componentMap = createComponentMapFeatures(locale, f);
                    final int zoom = MapPdfRemoteService.zoomLayer(componentMap.getBbox(), parameters);
                    final FeatureCollection componentApproachMap =
                            createApplicationAreaMapFeatures(firstApplicationArea, secondApplicationArea, componentMap);
                    final int approachMapZoom = MapPdfRemoteService.zoomLayer(componentApproachMap.getBbox(), parameters);

                    return new PrintApplicationApproachMapFeatureCollection(
                            componentMap,
                            baseMap + zoom,
                            componentApproachMap,
                            baseMap + approachMapZoom,
                            paperSize,
                            orientation);
                }).collect(toList());

        final List<PrintApplicationConflictMapModel.PropertyInfo> propertyNumberList = features.getFeatures().stream()
                .map(f -> {
                    final String propertyNumber = getNullableProperty(f, GeoJSONConstants.PROPERTY_NUMBER);
                    final double propertyAreaSize = getDoubleProperty(f, GeoJSONConstants.PROPERTY_SIZE);
                    final double propertyWaterAreaSize = getDoubleProperty(f, GeoJSONConstants.PROPERTY_WATER_AREA_SIZE);

                    final String propertyNumberDelimited = PropertyIdentifier.create(propertyNumber).getDelimitedValue();
                    final String propertyName = getNullableProperty(f, GeoJSONConstants.PROPERTY_NAME);

                    final Long id = Long.parseLong(f.getId());
                    final double propertyConflictAreaSize = conflictSummaries.containsKey(id) ? conflictSummaries.get(id).getConflictSum() : 0;
                    final double propertyConflictWaterAreaSize = conflictSummaries.containsKey(id) ? conflictSummaries.get(id).getConflictWaterSum() : 0;

                    return new PrintApplicationConflictMapModel.PropertyInfo(propertyNumberDelimited, propertyName,
                            propertyAreaSize,
                            propertyWaterAreaSize,
                            propertyConflictAreaSize,
                            propertyConflictWaterAreaSize);
                })
                .distinct()
                .sorted(comparing(PrintApplicationConflictMapModel.PropertyInfo::getPropertyNumber))
                .collect(toList());

        final String firstPermitHolder = F.mapNullable(firstApplication.getPermitHolder(), PermitHolder::getName);
        final String secondPermitHolder = F.mapNullable(secondApplication.getPermitHolder(), PermitHolder::getName);

        return new PrintApplicationConflictMapModel(
                unionFeatures,
                componentFeatures,
                propertyNumberList,
                firstPermitHolder,
                secondPermitHolder);
    }

    private static FeatureCollection createComponentMapFeatures(final Locale locale, final Feature f) {
        final Geometry geometry = PolygonConversionUtil.geoJsonToJava(f.getGeometry(), GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds bounds = GISBounds.create(geometry.getEnvelopeInternal());
        final String propertyNumber = getNullableProperty(f, GeoJSONConstants.PROPERTY_NUMBER);
        final String propertyNumberDelimited = PropertyIdentifier.create(propertyNumber).getDelimitedValue();
        final String propertyName = getNullableProperty(f, GeoJSONConstants.PROPERTY_NAME);
        final String propertyId = f.getId();

        final MapPdfModel model = new MapPdfModel.Builder(locale)
                .withClubName(LocalisedString.of(propertyNumberDelimited + " - " + propertyId))
                .withAreaName(LocalisedString.of(propertyName))
                .withBbox(bounds.toBBox())
                .withGeometry(geometry)
                .build();

        return model.getFeatures();
    }

    private static String getNullableProperty(final Feature f, final String propertyName) {
        return Optional.<String>ofNullable(f.getProperty(propertyName)).orElse("");
    }

    private static double getDoubleProperty(final Feature f, final String propertyName) {
        try {
            return f.getProperty(propertyName);
        } catch (
                final NullPointerException | NumberFormatException | ClassCastException ignore) {
            return 0;
        }
    }

    private static FeatureCollection createUnionMapFeatures(final Locale locale, final FeatureCollection features) {
        final List<Geometry> geometryList = features.getFeatures().stream()
                .map(f -> PolygonConversionUtil.geoJsonToJava(f.getGeometry(), GISUtils.SRID.ETRS_TM35FIN))
                .collect(toList());

        final Geometry unionGeometry = GISUtils.computeUnionFaster(geometryList);
        final GISBounds unionBounds = GISBounds.create(unionGeometry.getEnvelopeInternal());

        final MapPdfModel model = new MapPdfModel.Builder(locale)
                .withClubName(LocalisedString.of("Konfliktialueen kartta"))
                .withAreaName(LocalisedString.of("Tulostettu " + DATE_FORMAT.print(DateUtil.now())))
                .withBbox(unionBounds.toBBox())
                .withGeometry(unionGeometry)
                .build();

        return model.getFeatures();
    }

    private FeatureCollection createApplicationAreaMapFeatures(final HarvestPermitArea firstApplicationArea,
                                                               final HarvestPermitArea secondApplicationArea,
                                                               final FeatureCollection approachMapFeatures) {
        final Geometry firstApplicationGeometry = Optional.ofNullable(firstApplicationArea)
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> zoneRepository.getSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN))
                .orElse(null);

        final Feature firstApplicationFeature = new Feature();
        firstApplicationFeature.setGeometry(PolygonConversionUtil.javaToGeoJSON(firstApplicationGeometry));
        firstApplicationFeature.setProperty("fill", "rgb(0, 255, 0)");
        firstApplicationFeature.setProperty("fill-opacity", 0.4);
        firstApplicationFeature.setProperty("stroke-width", 1.0);
        firstApplicationFeature.setProperty("stroke", "rgb(0,0,0)");

        final Geometry secondApplicaitonGeometry = Optional.ofNullable(secondApplicationArea)
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> zoneRepository.getSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN))
                .orElse(null);

        final Feature secondApplicationFeature = new Feature();
        secondApplicationFeature.setGeometry(PolygonConversionUtil.javaToGeoJSON(secondApplicaitonGeometry));
        secondApplicationFeature.setProperty("fill", "rgb(0, 0, 255)");
        secondApplicationFeature.setProperty("fill-opacity", 0.3);
        secondApplicationFeature.setProperty("stroke-width", 1.0);
        secondApplicationFeature.setProperty("stroke", "rgb(0,0,0)");

        final Geometry unionGeometry = GISUtils.computeUnionFaster(Arrays.asList(firstApplicationGeometry, secondApplicaitonGeometry));
        final GISBounds unionBounds = GISBounds.create(unionGeometry.getEnvelopeInternal());

        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
        featureCollection.setBbox(unionBounds.toBBox());
        featureCollection.add(firstApplicationFeature);
        featureCollection.add(secondApplicationFeature);

        for (final Feature approachMapFeature : approachMapFeatures.getFeatures()) {
            final Feature feature = new Feature();
            feature.setGeometry(approachMapFeature.getGeometry());
            feature.setCrs(approachMapFeature.getCrs());
            feature.setBbox(approachMapFeature.getBbox());
            feature.setProperty("fill", "rgb(255, 0, 0)");
            feature.setProperty("fill-opacity", 1.0);
            feature.setProperty("stroke-width", 1.0);
            feature.setProperty("stroke", "rgb(0,0,0)");
            featureCollection.add(feature);
        }

        return featureCollection;
    }

    private String i18n(final Locale locale, final String localisationKey) {
        return messageSource.getMessage(LOCALIZATION_KEY_PREFIX + localisationKey, null, locale);
    }
}
