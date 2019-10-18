package fi.riista.feature.permit.application.fragment;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.geojson.Feature;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PrintApplicationAreaFragmentFeature {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00", new DecimalFormatSymbols(Locales.FI));

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

    public byte[] createPdf(final PrintApplicationAreaFragmentDTO fragmentData,
                            final MapPdfModel mapPdfModel,
                            final MapPdfParameters mapParameters) {
        final byte[] mapPdfData = mapPdfRemoteService.renderPdf(mapParameters, mapPdfModel);

        try (final InputStream is = new ByteArrayInputStream(mapPdfData);
             final PDDocument pdfDocument = PDDocument.load(is)) {

            final List<HarvestPermitAreaFragmentPropertyDTO> propertyList = fragmentData.getFragmentProperties().stream()
                    .sorted(Comparator.comparing(HarvestPermitAreaFragmentPropertyDTO::getPropertyNumber))
                    .collect(Collectors.toList());

            for (List<HarvestPermitAreaFragmentPropertyDTO> partition : Lists.partition(propertyList, 50)) {
                renderPageOfProperties(pdfDocument, partition);
            }

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            pdfDocument.save(bos);
            pdfDocument.close();
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderPageOfProperties(final PDDocument pdfDocument,
                                        final List<HarvestPermitAreaFragmentPropertyDTO> propertyList) throws IOException {
        final PDPage pdfPage = new PDPage(PDRectangle.A4);
        pdfDocument.addPage(pdfPage);

        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            writer.topOffsetMm(15).marginLeftMm(15);

            for (HarvestPermitAreaFragmentPropertyDTO fragmentProperty : propertyList) {
                writer.writeLine(fragmentProperty.getPropertyNumber());
            }

            writer.topOffsetMm(15).marginLeftMm(65);

            for (HarvestPermitAreaFragmentPropertyDTO fragmentProperty : propertyList) {
                final String sizeText = DECIMAL_FORMAT.format(fragmentProperty.getPropertyArea() / 10_000);
                writer.writeLine(sizeText + " ha");
            }

            writer.topOffsetMm(15).marginLeftMm(100);

            for (HarvestPermitAreaFragmentPropertyDTO fragmentProperty : propertyList) {
                if (StringUtils.hasText(fragmentProperty.getPropertyName())) {
                    writer.writeLine(fragmentProperty.getPropertyName());
                } else {
                    writer.writeEmptyLine();
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public PrintApplicationAreaFragmentDTO getFragmentData(final long id, final String fragmentId) {
        final HarvestPermitApplication application = getApplication(id);
        final long zoneId = requireZoneId(application);

        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        final int fragmentSizeLimit = 1000 * 10000;

        // Lookup fragment area size
        final GISWGS84Point location = geoHashToLocationService.convert(fragmentId);
        final HarvestPermitAreaFragmentQueryParams queryParams = new HarvestPermitAreaFragmentQueryParams(
                zoneId, latestHirviYear, fragmentSizeLimit, location);

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

    @Transactional(readOnly = true)
    public MapPdfModel getMapModel(final PrintApplicationAreaFragmentDTO dto,
                                   final MapPdfParameters.Overlay overlayType,
                                   final Locale locale) {
        final GISZoneSizeDTO areaSize = new GISZoneSizeDTO(
                dto.getFragmentSize().getBothSize(),
                dto.getFragmentSize().getStateSize().getTotal(),
                dto.getFragmentSize().getPrivateSize().getTotal());

        // Lookup fragment polygon by geohash
        final Feature fragmentFeature = gisZoneRepository
                .getCombinedPolygonFeatures(dto.getZoneId(), GISUtils.SRID.ETRS_TM35FIN)
                .getFeatures().stream()
                .filter(feature -> dto.getFragmentId().equals(feature.getProperty(GeoJSONConstants.PROPERTY_HASH)))
                .findFirst().orElseThrow(IllegalArgumentException::new);

        final Geometry overlayGeometry = overlayType == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(dto.getZoneId(), GISUtils.SRID.ETRS_TM35FIN)
                : null;

        return new MapPdfModel.Builder(locale)
                .withExternalId(dto.getFragmentId())
                .withAreaName(LocalisedString.of(dto.getApplicationName()))
                .withClubName(LocalisedString.of(dto.getPermitHolderName()))
                .withModificationTime(dto.getApplicationSubmitDate().toDate())
                .withAreaSize(areaSize)
                .withBbox(fragmentFeature.getBbox())
                .withGeometry(fragmentFeature.getGeometry())
                .withOverlayGeometry(overlayGeometry)
                .build();
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

}
