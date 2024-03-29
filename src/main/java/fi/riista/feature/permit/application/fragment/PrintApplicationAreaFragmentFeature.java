package fi.riista.feature.permit.application.fragment;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PrintApplicationApproachMapFeatureCollection;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.PdfWriter;
import fi.riista.util.PolygonConversionUtil;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class PrintApplicationAreaFragmentFeature {

    private static final int FRAGMENT_SIZE_LIMIT_MOOSE_HECTARES = 1000 * 10000;
    private static final String LOCALIZATION_KEY_PREFIX = "PrintApplicationAreaFragmentFeature.";

    public static class PdfData {
        private final String filename;
        private final byte[] data;

        public String getFilename() {
            return filename;
        }

        public byte[] getData() {
            return data;
        }

        private PdfData(final String filename, final byte[] data) {
            this.filename = filename;
            this.data = data;
        }
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00", new DecimalFormatSymbols(Locales.FI));
    private static final long MAX_MAIN_MEM_PDF_MERGE = 15 * 2 ^ 20; //15 MB

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private GeoHashToLocationService geoHashToLocationService;

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

    @Resource
    private HarvestPermitAreaFragmentRepository harvestPermitAreaFragmentRepository;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public PdfData makeConcatenatedPdf(final long applicationId,
                                       final PrintApplicationAreaFragmentListDTO fragmentsDto,
                                       final Locale locale) throws IOException {
        final HarvestPermitApplication application = getApplication(applicationId);
        final List<PrintApplicationAreaFragmentDTO> fragmentsData = getFragmentsData(application,
                fragmentsDto.getFragmentIds());

        Preconditions.checkState(!fragmentsData.isEmpty(), "Area contains no fragments.");
        final AtomicReference<String> fileName = new AtomicReference<>(null);

        final List<InputStream> bais = fragmentsData.stream()
                .map(fragmentData -> {
                    final PrintApplicationAreaFragmentMapModel mapModel = getMapModelWithApproachMap(applicationId,
                            fragmentData,
                            fragmentsDto.getPdfParameters(), locale);
                    fileName.compareAndSet(null, fragmentData.getApplicationName() + fragmentData.getFragmentId());

                    return createSingleFragmentWithApproachPdf(fragmentData, mapModel, locale);
                })
                .map(ByteArrayInputStream::new)
                .collect(Collectors.toList());


        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

        // Overview page if multiple fragments are printed
        if (fragmentsData.size() > 1) {
            final byte[] overviewPdf =
                    createOverviewPdf(application, fragmentsData, fragmentsDto.getPdfParameters(), locale);
            pdfMergerUtility.addSource(new ByteArrayInputStream(overviewPdf));
        }

        pdfMergerUtility.addSources(bais);
        pdfMergerUtility.setDestinationStream(bos);

        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(MAX_MAIN_MEM_PDF_MERGE));

        return new PdfData(requireNonNull(fileName.get()), bos.toByteArray());

    }

    @Transactional(readOnly = true)
    public PrintApplicationAreaFragmentMapModel getMapModelWithApproachMap(final long applicationId,
                                                                                   final PrintApplicationAreaFragmentDTO dto,
                                                                                   final MapPdfParameters parameters,
                                                                                   final Locale locale) {
        final MapPdfModel fragmentModel = getMapModel(dto, parameters.getOverlay(), locale);

        final FeatureCollection fragmentFeatureCollection = fragmentModel.getFeatures();
        final Feature fragmentFeature = fragmentFeatureCollection.getFeatures().get(0);

        final Feature fragmentApproachFeature = new Feature();
        fragmentApproachFeature.setGeometry(fragmentFeature.getGeometry());
        fragmentApproachFeature.setBbox(fragmentFeature.getBbox());
        fragmentApproachFeature.setCrs(fragmentFeature.getCrs());
        fragmentApproachFeature.setProperty("fill", "rgb(255, 0, 0)");
        fragmentApproachFeature.setProperty("fill-opacity", 1.0);
        fragmentApproachFeature.setProperty("stroke-width", 1.0);
        fragmentApproachFeature.setProperty("stroke", "rgb(0,0,0)");

        final int mapZoom = MapPdfRemoteService.zoomLayer(fragmentFeatureCollection.getBbox(), parameters);

        final FeatureCollection approachMapFeatures = getApproachMapFeatures(applicationId);
        approachMapFeatures.add(fragmentApproachFeature);

        final int approachMapZoom = MapPdfRemoteService.zoomLayer(approachMapFeatures.getBbox(), parameters);

        final PrintApplicationApproachMapFeatureCollection featureCollection =
                new PrintApplicationApproachMapFeatureCollection(
                        fragmentModel.getFeatures(),
                        parameters.getLayer().getName() + mapZoom,
                        approachMapFeatures,
                        parameters.getLayer().getName() + approachMapZoom,
                        parameters.getPaperSize().name(),
                        parameters.getPaperOrientation().asLetter());

        return new PrintApplicationAreaFragmentMapModel(featureCollection, fragmentModel.getExportFileName());
    }

    public byte[] createSingleFragmentWithApproachPdf(final PrintApplicationAreaFragmentDTO fragmentData,
                                                      final PrintApplicationAreaFragmentMapModel mapPdfModel,
                                                      final Locale locale) {
        final byte[] mapPdfData = mapPdfRemoteService.renderApproachMapPdf(mapPdfModel.getFeatureCollection());

        try (final InputStream is = new ByteArrayInputStream(mapPdfData);
             final PDDocument pdfDocument = PDDocument.load(is)) {

            final List<HarvestPermitAreaFragmentPropertyDTO> propertyList =
                    fragmentData.getFragmentProperties().stream()
                            .sorted(Comparator.comparing(HarvestPermitAreaFragmentPropertyDTO::getPropertyNumber))
                            .collect(Collectors.toList());

            for (final List<HarvestPermitAreaFragmentPropertyDTO> partition : Lists.partition(propertyList, 45)) {
                renderPageOfProperties(pdfDocument, fragmentData.getFragmentId(), partition, locale);
            }

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            pdfDocument.save(bos);
            pdfDocument.close();
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] createOverviewPdf(final HarvestPermitApplication application,
                                     final List<PrintApplicationAreaFragmentDTO> fragmentDatas,
                                     final MapPdfParameters mapParameters, final Locale locale) {
        final long zoneId = requireZoneId(application);

        final ArrayList<String> hashes = F.mapNonNullsToList(fragmentDatas,
                PrintApplicationAreaFragmentDTO::getFragmentId);

        final List<Geometry> geometryList = gisZoneRepository
                .getCombinedPolygonFeatures(zoneId, GISUtils.SRID.ETRS_TM35FIN).getFeatures().stream()
                .filter(feature -> hashes.contains(feature.getProperty(GeoJSONConstants.PROPERTY_HASH)))
                .map(f -> PolygonConversionUtil.geoJsonToJava(f.getGeometry(), GISUtils.SRID.ETRS_TM35FIN))
                .collect(toList());

        final Geometry geometry = GISUtils.computeUnionFaster(geometryList);
        final GISBounds bounds = GISBounds.create(geometry.getEnvelopeInternal());


        final Geometry overlayGeometry = mapParameters.getOverlay() == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(calculateOverlayBox(bounds.toBBox()), GISUtils.SRID.ETRS_TM35FIN)
                : null;

        final MapPdfModel mapPdfModel = new MapPdfModel.Builder(locale)
                .withAreaName(LocalisedString.of(application.getApplicationName()))
                .withClubName(LocalisedString.of(application.getPermitHolder().getName()))
                .withExternalId(i18n(locale, "fragmentOverviewTitle"))
                .withModificationTime(application.getSubmitDate())
                .withBbox(bounds.toBBox())
                .withGeometry(geometry)
                .withOverlayGeometry(overlayGeometry)
                .build();

        return mapPdfRemoteService.renderPdf(mapParameters, mapPdfModel);
    }

    private void renderPageOfProperties(final PDDocument pdfDocument,
                                        final String fragmentId,
                                        final List<HarvestPermitAreaFragmentPropertyDTO> propertyList,
                                        final Locale locale) throws IOException {
        final Iterator<HarvestPermitAreaFragmentPropertyDTO> propertyIterator = propertyList.iterator();

        while (propertyIterator.hasNext()) {
            final PDPage pdfPage = new PDPage(PDRectangle.A4);
            pdfDocument.addPage(pdfPage);

            try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
                writer.topOffsetMm(15).marginLeftMm(15);
                writer.writeLine(i18n(locale, "fragmentIdentifier") + " " + fragmentId);

                writer.topOffsetMm(25);
                writer.marginLeftMm(65).write(i18n(locale, "totalArea") + " ")
                        .marginLeftMm(105).write(i18n(locale, "landArea") + " ")
                        .marginLeftMm(135).write(i18n(locale, "waterArea") + " ")
                        .writeEmptyLine().writeEmptyLine();

                while (propertyIterator.hasNext()) {
                    final HarvestPermitAreaFragmentPropertyDTO fragmentProperty = propertyIterator.next();

                    writer.marginLeftMm(15);
                    writer.write(fragmentProperty.getPropertyNumber());

                    writer.marginLeftMm(65);
                    final String totalArea = DECIMAL_FORMAT.format(fragmentProperty.getPropertyArea() / 10_000);
                    writer.write(totalArea + " ha");

                    writer.marginLeftMm(105);
                    final String landArea = DECIMAL_FORMAT.format(fragmentProperty.getPropertyLandArea() / 10_000);
                    writer.write(landArea + " ha");

                    writer.marginLeftMm(135);
                    final String waterArea = DECIMAL_FORMAT.format(fragmentProperty.getPropertyWaterArea() / 10_000);
                    writer.writeLine(waterArea + " ha");

                    if (StringUtils.hasText(fragmentProperty.getPropertyName())) {
                        writer.marginLeftMm(25);
                        writer.italicFont().writeParagraph(fragmentProperty.getPropertyName(), 95f);
                    }

                    writer.normalFont().writeEmptyLine();

                    final float sizeNeededForContent = 3 * writer.getLineHeight() + writer.getMarginFromMm(15);
                    if ((writer.getPosY() - sizeNeededForContent) < 0) {
                        break;
                    }
                }
            }
        }
    }

    private String i18n(final Locale locale, final String localisationKey) {
        return messageSource.getMessage(LOCALIZATION_KEY_PREFIX + localisationKey, null, locale);
    }

    @Transactional(readOnly = true)
    public PrintApplicationAreaFragmentDTO getFragmentData(final long id, final String fragmentId) {
        final HarvestPermitApplication application = getApplication(id);
        final long zoneId = requireZoneId(application);

        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        final int fragmentSizeLimitHa = FRAGMENT_SIZE_LIMIT_MOOSE_HECTARES;

        // Lookup fragment area size
        final GISWGS84Point location = geoHashToLocationService.convert(fragmentId);
        final HarvestPermitAreaFragmentQueryParams queryParams = new HarvestPermitAreaFragmentQueryParams(
                zoneId, latestHirviYear, fragmentSizeLimitHa, location);

        final HarvestPermitAreaFragmentSizeDTO fragmentSizeDTO = harvestPermitAreaFragmentRepository
                .getFragmentSize(queryParams)
                .stream().filter(dto -> fragmentId.equals(dto.getHash()))
                .findFirst().orElseThrow(IllegalArgumentException::new);

        final List<HarvestPermitAreaFragmentPropertyDTO> fragmentPropertyNumbers =
                harvestPermitAreaFragmentRepository.getFragmentProperty(queryParams).get(fragmentId);

        return new PrintApplicationAreaFragmentDTO(zoneId, fragmentId, application.getApplicationName(),
                application.getSubmitDate(), application.getPermitHolder().getName(),
                fragmentSizeDTO, fragmentPropertyNumbers);
    }

    private List<PrintApplicationAreaFragmentDTO> getFragmentsData(final HarvestPermitApplication application,
                                                                   final List<String> fragmentIds) {
        final long zoneId = requireZoneId(application);

        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        final int fragmentSizeLimitHa = FRAGMENT_SIZE_LIMIT_MOOSE_HECTARES;

        final HarvestPermitAreaFragmentQueryParams queryParams = new HarvestPermitAreaFragmentQueryParams(
                zoneId, latestHirviYear, fragmentSizeLimitHa, null);

        final List<HarvestPermitAreaFragmentSizeDTO> fragmentSizeDTOS = harvestPermitAreaFragmentRepository
                .getFragmentSize(queryParams, fragmentIds);

        final Map<String, List<HarvestPermitAreaFragmentPropertyDTO>> fragmentPropertyNumbers =
                harvestPermitAreaFragmentRepository.getFragmentProperty(queryParams);

        return F.mapNonNullsToList(fragmentSizeDTOS, f -> new PrintApplicationAreaFragmentDTO(zoneId, f.getHash(),
                application.getApplicationName(), application.getSubmitDate(),
                application.getPermitHolder().getName(), f, fragmentPropertyNumbers.get(f.getHash())));
    }

    private HarvestPermitApplication getApplication(final long applicationId) {
        return requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
    }

    private static long requireZoneId(final HarvestPermitApplication application) {
        return Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .orElseThrow(() -> new IllegalArgumentException("Permit area is missing"));
    }

    // Return geometry that will be at least the area of the visible map
    private static Geometry calculateOverlayBox(final double[] bbox) {
        Preconditions.checkArgument(bbox.length == 4);
        final double minLng = bbox[0];
        final double minLat = bbox[1];
        final double maxLng = bbox[2];
        final double maxLat = bbox[3];

        Preconditions.checkArgument(Double.compare(maxLng, minLng) >= 0);
        Preconditions.checkArgument(Double.compare(maxLat, minLat) >= 0);

        final double diffLng = maxLng - minLng;
        final double diffLat = maxLat - minLat;

        final double midLng = maxLng - diffLng / 2;
        final double midLat = maxLat - diffLat / 2;

        // Paper ratio is 1/sqrt(2), doubling the dimensions based on the
        // larger one is enough to cover the visible area on map
        final double increaseAmount = Math.max(diffLat, diffLng);
        return GISUtils.createPolygon(
                new GISBounds(midLng - increaseAmount,
                        midLat - increaseAmount,
                        midLng + increaseAmount,
                        midLat + increaseAmount),
                GISUtils.SRID.ETRS_TM35FIN);
    }

    private MapPdfModel getMapModel(final PrintApplicationAreaFragmentDTO dto,
                                    final MapPdfParameters.Overlay overlay,
                                    final Locale locale) {
        final GISZoneSizeDTO areaSize = GISZoneSizeDTO.create(
                dto.getFragmentSize().getBothSize(),
                dto.getFragmentSize().getStateSize().getTotal(),
                dto.getFragmentSize().getPrivateSize().getTotal());

        // Lookup fragment polygon by geohash
        final Feature fragmentFeature = gisZoneRepository
                .getCombinedPolygonFeatures(dto.getZoneId(), GISUtils.SRID.ETRS_TM35FIN)
                .getFeatures().stream()
                .filter(feature -> dto.getFragmentId().equals(feature.getProperty(GeoJSONConstants.PROPERTY_HASH)))
                .findFirst().orElseThrow(IllegalArgumentException::new);

        final Geometry overlayGeometry = overlay == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(calculateOverlayBox(fragmentFeature.getBbox()),
                        GISUtils.SRID.ETRS_TM35FIN)
                : null;

        return new MapPdfModel.Builder(locale)
                .withExternalId(dto.getFragmentId())
                .withAreaName(LocalisedString.of(dto.getApplicationName()))
                .withClubName(LocalisedString.of(dto.getPermitHolderName()))
                .withModificationTime(dto.getApplicationSubmitDate())
                .withAreaSize(areaSize)
                .withBbox(fragmentFeature.getBbox())
                .withGeometry(fragmentFeature.getGeometry())
                .withOverlayGeometry(overlayGeometry)
                .build();
    }

    private FeatureCollection getApproachMapFeatures(final long applicationId) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
        final Geometry applicationGeometry = Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> gisZoneRepository.getSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN))
                .orElse(null);
        final GISBounds bounds = gisZoneRepository.getBounds(application.getArea().getZone().getId(), GISUtils.SRID.ETRS_TM35FIN);

        final Feature applicationFeature = new Feature();
        applicationFeature.setGeometry(PolygonConversionUtil.javaToGeoJSON(applicationGeometry));
        applicationFeature.setProperty("fill", "rgb(0, 255, 0)");
        applicationFeature.setProperty("fill-opacity", 0.4);
        applicationFeature.setProperty("stroke-width", 1.0);
        applicationFeature.setProperty("stroke", "rgb(0,0,0)");

        final FeatureCollection approachMapFeatures = new FeatureCollection();
        approachMapFeatures.setCrs(GISUtils.SRID.ETRS_TM35FIN.getGeoJsonCrs());
        approachMapFeatures.setBbox(bounds.toBBox());
        approachMapFeatures.add(applicationFeature);

        return approachMapFeatures;
    }
}
