package fi.riista.feature.harvestpermit.area;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class HarvestPermitAreaPartnersExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String HEADER_PREFIX = "HarvestPermitAreaPartnersExcel.";
    private static final String[] HEADERS = {"club", "area", "externalId", "land", "water", "total",};

    private final List<HarvestPermitAreaPartnerDTO> partners;
    private final EnumLocaliser localiser;
    private final String externalId;
    private final Locale locale;

    public HarvestPermitAreaPartnersExcelView(final EnumLocaliser localiser,
                                              final Locale locale,
                                              final String externalId,
                                              final List<HarvestPermitAreaPartnerDTO> partners) {
        this.localiser = localiser;
        this.locale = locale;
        this.externalId = externalId;
        this.partners = partners;
    }

    private String createFilename() {
        return String.format(
                "%s-%s-%s.xls",
                localiser.getTranslation(HEADER_PREFIX + "partners"),
                externalId,
                DATETIME_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        response.setHeader(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(HEADER_PREFIX + "partners"));
        helper.appendHeaderRow(createHeaderRow());

        for (HarvestPermitAreaPartnerDTO partner : partners) {
            helper.appendRow();

            helper.appendTextCell(partner.getClub().getNameLocalisation().getAnyTranslation(locale));
            helper.appendTextCell(partner.getSourceArea().getName().getOrDefault(locale.getLanguage(), Locales.FI_LANG));
            helper.appendTextCell(partner.getSourceArea().getExternalId());

            appendSquareMetersInHectares(helper, partner.getTotalAreaSize() - partner.getWaterAreaSize());
            appendSquareMetersInHectares(helper, partner.getWaterAreaSize());
            appendSquareMetersInHectares(helper, partner.getTotalAreaSize());
        }
        helper.autoSizeColumns();
    }

    private ExcelHelper appendSquareMetersInHectares(ExcelHelper helper, double squareMeters) {
        return helper.appendDoubleCell(squareMeters / 10_000, 2);
    }

    private String[] createHeaderRow() {
        return Stream.of(HEADERS)
                .map(key -> localiser.getTranslation(HEADER_PREFIX + key))
                .toArray(String[]::new);
    }
}
