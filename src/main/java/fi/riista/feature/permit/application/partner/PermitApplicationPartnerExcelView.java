package fi.riista.feature.permit.application.partner;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class PermitApplicationPartnerExcelView extends AbstractXlsxView {

    private static final String HEADER_PREFIX = "HarvestPermitAreaPartnersExcel.";
    private static final String[] HEADERS = {
            "club", "area", "externalId", "land", "water", "total",
    };

    private final List<HarvestPermitAreaPartnerDTO> partners;
    private final EnumLocaliser localiser;
    private final String externalId;

    public PermitApplicationPartnerExcelView(final EnumLocaliser localiser,
                                             final String externalId,
                                             final List<HarvestPermitAreaPartnerDTO> partners) {
        this.localiser = localiser;
        this.externalId = externalId;
        this.partners = partners;
    }

    private String createFilename() {
        return String.format(
                "%s-%s-%s.xlsx",
                localiser.getTranslation(HEADER_PREFIX + "partners"),
                externalId,
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(HEADER_PREFIX + "partners"))
                .appendHeaderRow(localiser.translate(HEADER_PREFIX, HEADERS));

        for (final HarvestPermitAreaPartnerDTO partner : partners) {
            helper.appendRow()
                    .appendTextCell(localiser.getTranslation(partner.getClub().getNameLocalisation()))
                    .appendTextCell(localiser.getTranslation(LocalisedString.fromMap(partner.getSourceArea().getName())))
                    .appendTextCell(partner.getSourceArea().getExternalId());

            appendSquareMetersInHectares(helper, partner.getSize().getLand());
            appendSquareMetersInHectares(helper, partner.getSize().getWater());
            appendSquareMetersInHectares(helper, partner.getSize().getTotal());
        }
        helper.autoSizeColumns();
    }

    private static void appendSquareMetersInHectares(final ExcelHelper helper, final double squareMeters) {
        helper.appendDoubleCell(squareMeters / 10_000, 2);
    }
}
