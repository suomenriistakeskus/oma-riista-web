package fi.riista.feature.account.area.union;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerService;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import fi.riista.util.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Component
public class PersonalAreaUnionPrintService {

    private static final int SQUARE_METERS_TO_HECTARES = 10_000;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private HarvestPermitAreaPartnerService harvestPermitAreaPartnerService;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MapPdfModel getModel(final PersonalAreaUnion union, final MapPdfParameters.Overlay overlayType,
                                final Locale locale) {
        final HarvestPermitArea harvestPermitArea = union.getHarvestPermitArea();

        Preconditions.checkState(harvestPermitArea.getStatus() == HarvestPermitArea.StatusCode.READY);

        final long zoneId = harvestPermitArea.getZone().getId();

        // NOTE: Must retrieve original bounds because inverted geometry bounding box contains all of Finland
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(zoneId);
        final Geometry geometry = gisZoneRepository.getInvertedSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final Geometry overlayGeometry = overlayType == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN)
                : null;

        return new MapPdfModel.Builder(locale)
                .withExternalId(harvestPermitArea.getExternalId())
                .withAreaName(LocalisedString.of(union.getName()))
                .withClubName(LocalisedString.of(union.getName()))
                .withModificationTime(union.getModificationTime())
                .withAreaSize(areaSize)
                .withGeometry(geometry)
                .withOverlayGeometry(overlayGeometry)
                .withBbox(bounds.toBBox())
                .build();
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public byte[] createPdf(final PersonalAreaUnion union, final Locale locale,
                            final MapPdfModel mapPdfModel,
                            final MapPdfParameters dto) {
        final HarvestPermitArea harvestPermitArea = union.getHarvestPermitArea();

        Preconditions.checkState(harvestPermitArea.getStatus() == HarvestPermitArea.StatusCode.READY);

        final byte[] mapPdfData = mapPdfRemoteService.renderPdf(dto, mapPdfModel);

        try (final InputStream is = new ByteArrayInputStream(mapPdfData);
             final PDDocument pdfDocument = PDDocument.load(is)) {

            final List<HarvestPermitAreaPartnerDTO> partners =
                    harvestPermitAreaPartnerService.listPartners(harvestPermitArea, locale);

            for (List<HarvestPermitAreaPartnerDTO> partition : Lists.partition(partners, 8)) {
                renderPageOfPartners(pdfDocument, locale, partition);
            }

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            pdfDocument.save(bos);
            pdfDocument.close();
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderPageOfPartners(final PDDocument pdfDocument, final Locale locale,
                                      final List<HarvestPermitAreaPartnerDTO> partition) throws IOException {
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, locale);

        // A4 landscape
        final PDPage pdfPage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));

        pdfDocument.addPage(pdfPage);

        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            writer.topOffsetMm(15).marginLeftMm(15);

            writer.boldFont().writeLine(enumLocaliser.getTranslation("AccountAreaUnion.clubName"));
            writer.writeEmptyLine();
            writer.writeEmptyLine();
            writer.normalFont();
            for (HarvestPermitAreaPartnerDTO partner : partition) {
                writer.writeLine(partner.getClub().getNameLocalisation().getTranslation(locale));
                writer.writeEmptyLine();
                writer.writeEmptyLine();
                writer.writeEmptyLine();
            }

            writer.topOffsetMm(15).marginLeftMm(120);

            writer.boldFont().writeLine(enumLocaliser.getTranslation("AccountAreaUnion.clubAreaName"));
            writer.writeEmptyLine();
            writer.writeEmptyLine();
            writer.normalFont();
            for (HarvestPermitAreaPartnerDTO partner : partition) {
                writer.writeLine(partner.getSourceArea().getName().get(locale.getLanguage()));
                writer.writeEmptyLine();
                writer.writeEmptyLine();
                writer.writeEmptyLine();
            }

            writer.topOffsetMm(15).marginLeftMm(190);

            writer.boldFont().writeLine(enumLocaliser.getTranslation("AccountAreaUnion.areaExternalId"));
            writer.writeEmptyLine();
            writer.writeEmptyLine();
            writer.normalFont();
            for (HarvestPermitAreaPartnerDTO partner : partition) {
                writer.writeLine(partner.getSourceArea().getExternalId());
                writer.writeEmptyLine();
                writer.writeEmptyLine();
                writer.writeEmptyLine();
            }
            writer.topOffsetMm(15).marginLeftMm(230);
            writer.boldFont().writeLine(enumLocaliser.getTranslation("AccountAreaUnion.landAreaSize"));
            writer.boldFont().writeLine(enumLocaliser.getTranslation("AccountAreaUnion.waterAreaSize"));
            writer.boldFont().writeLine(enumLocaliser.getTranslation("AccountAreaUnion.totalAreaSize"));
            writer.normalFont();

            for (HarvestPermitAreaPartnerDTO partner : partition) {
                writer.writeLine(String.valueOf(Math.round(partner.getSize().getLand() / SQUARE_METERS_TO_HECTARES)));
                writer.writeLine(String.valueOf(Math.round(partner.getSize().getWater() / SQUARE_METERS_TO_HECTARES)));
                writer.writeLine(String.valueOf(Math.round(partner.getSize().getTotal() / SQUARE_METERS_TO_HECTARES)));
                writer.writeEmptyLine();
            }
        }
    }

}
