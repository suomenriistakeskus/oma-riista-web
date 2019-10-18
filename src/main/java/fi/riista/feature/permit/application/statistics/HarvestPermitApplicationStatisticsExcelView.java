package fi.riista.feature.permit.application.statistics;


import com.google.common.collect.ImmutableList;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class HarvestPermitApplicationStatisticsExcelView extends AbstractXlsxView {

    private static final String I18N_PREFIX = "HarvestPermitApplicationStatisticsExcel.";
    private final EnumLocaliser i18n;
    private final List<HarvestPermitApplicationStatusTableDTO> statuses;

    public HarvestPermitApplicationStatisticsExcelView(final EnumLocaliser i18n,
                                                       final List<HarvestPermitApplicationStatusTableDTO> statuses) {
        this.i18n = i18n;
        this.statuses = statuses;
    }

    private String createFilename() {
        return String.format("Hakemukset-tilanne-%s.xlsx", Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook);
    }

    private void createSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, "Hakemusten käsittelytilanne");
        helper.setDefaultColumnWidth(5);

        final List<String> permitCategories = getPermitCategories();

        addCategoryHeaderRow(helper, permitCategories);
        addStatusHeaderRow(helper, permitCategories);

        addStatusRows(helper, permitCategories);

        helper.autoSizeColumn(0);
    }

    private void addCategoryHeaderRow(ExcelHelper helper, List<String> permitCategories) {
        helper.appendRow();
        helper.appendEmptyCell(1);
        permitCategories.forEach(category -> {
            helper.appendTextCell(i18n.getTranslation(I18N_PREFIX + category), HorizontalAlignment.CENTER).spanCurrentColumn(3);
        });
    }

    private void addStatusHeaderRow(ExcelHelper helper, List<String> permitCategories) {
        helper.appendRow();
        helper.appendTextCell("RKA");

        permitCategories.forEach(category -> {
            helper.appendTextCell("H");
            helper.appendTextCell("K");
            helper.appendTextCell("V");
        });
    }

    private void addStatusRows(ExcelHelper helper, List<String> permitCategories) {
        statuses.forEach(rka -> {
            helper.appendRow();
            helper.appendTextCell(i18n.getTranslation(rka.getRka().getNameLocalisation()));
            permitCategories.forEach(category -> {
                appendCategoryNullable(helper, rka.getPermitCategoryToStatus().get(category));
            });
        });
    }

    private List<String> getPermitCategories() {
        return ImmutableList.of(
                HarvestPermitCategory.MOOSELIKE.name(),
                HarvestPermitCategory.MOOSELIKE_NEW.name(),
                HarvestPermitCategory.BIRD.name(),
                HarvestPermitCategory.LARGE_CARNIVORE_CATEGORIES
        );
    }

    private void appendCategoryNullable(final ExcelHelper helper, Map<String, Integer> categoryMap) {
        if (categoryMap == null) {
            helper.appendEmptyCell(3);
        } else {
            appendValueNullable(helper, categoryMap.get("H"));
            appendValueNullable(helper, categoryMap.get("K"));
            appendValueNullable(helper, categoryMap.get("V"));
        }
    }

    private void appendValueNullable(final ExcelHelper helper, final Integer value) {
        if (value == null) {
            helper.appendEmptyCell(1);
        } else {
            helper.appendNumberCell(value);
        }
    }
}
