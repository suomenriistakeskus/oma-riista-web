package fi.riista.feature.harvestpermit.report.jhtarchive;

import com.google.common.collect.Iterables;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JhtArchiveExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "JhtArchiveExcelView.";

    private final EnumLocaliser localiser;
    private final List<JhtArchiveExcelDTO> permitData;
    private final List<JhtArchiveExcelDTO> immaterialPermitData;
    private final String filenameDetails;

    public static JhtArchiveExcelView create(final EnumLocaliser localiser,
                                             final List<JhtArchiveExcelDTO> permitData,
                                             final List<JhtArchiveExcelDTO> immaterialPermitData,
                                             final String filenameDetails) {
        return new JhtArchiveExcelView(localiser, permitData, immaterialPermitData, filenameDetails);
    }

    protected JhtArchiveExcelView(final EnumLocaliser localiser,
                                  final List<JhtArchiveExcelDTO> permitData,
                                  final List<JhtArchiveExcelDTO> immaterialPermitData,
                                  final String filenameDetails) {
        this.localiser = localiser;
        this.permitData = permitData;
        this.immaterialPermitData = immaterialPermitData;
        this.filenameDetails = filenameDetails;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, getFilename());

        createPermitSheet(workbook);
        createImmaterialPermitSheet(workbook);
    }

    private String getFilename() {
        return String.format("%s%s.xlsx", getTranslation("filename"), filenameDetails);
    }

    private String getTranslation(final String localisationKey) {
        return localiser.getTranslation(LOCALISATION_PREFIX + localisationKey);
    }

    private void createPermitSheet(final Workbook workbook) {
        final String sheetName = getTranslation("permitSheetName");
        final ExcelHelper helper = new ExcelHelper(workbook, sheetName);
        final List<String> firstHeaderRow = Arrays.asList(
                "permitNumber",
                "permitType",
                "species",
                "applicationAmount",
                "skip",
                "skip",
                "skip",
                "permitAmount",
                "skip",
                "skip",
                "skip",
                "harvestAmount",
                "skip",
                "skip",
                "skip",
                "permitDates",
                "harvestReportState",
                "rka",
                "rhy"
        );
        final List<String> secondHeaderRow = Arrays.asList(
                "skip",
                "skip",
                "skip",
                // applicationAmount
                "specimenAmount",
                "nestAmount",
                "eggAmount",
                "constructionAmount",
                // permitAmount
                "specimenAmount",
                "nestAmount",
                "eggAmount",
                "constructionAmount",
                // harvestAmount
                "specimenAmount",
                "nestAmount",
                "eggAmount",
                "constructionAmount",
                "skip",
                "skip",
                "skip",
                "skip"
        );

        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, Iterables.toArray(firstHeaderRow, String.class)));
        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, Iterables.toArray(secondHeaderRow, String.class)));

        permitData.forEach(item -> helper.appendRow()
                .appendTextCell(item.getPermitNumber())
                .appendTextCell(localiser.getTranslation(item.getPermitType()))
                .appendTextCell(localiser.getTranslation(item.getSpecies()))
                .appendNumberCell(item.getApplicationSpecimenAmount())
                .appendNumberCell(item.getApplicationNestAmount())
                .appendNumberCell(item.getApplicationEggAmount())
                .appendNumberCell(item.getApplicationConstructionAmount())
                .appendNumberCell(item.getPermitSpecimenAmount())
                .appendNumberCell(item.getPermitNestAmount())
                .appendNumberCell(item.getPermitEggAmount())
                .appendNumberCell(item.getPermitConstructionAmount())
                .appendNumberCell(item.getHarvestSpecimenAmount())
                .appendNumberCell(item.getHarvestNestAmount())
                .appendNumberCell(item.getHarvestEggAmount())
                .appendNumberCell(item.getHarvestConstructionAmount())
                .appendTextCell(item.getPermitDates())
                .appendTextCell(Objects.equals(item.getMooselikeHuntingFinished(),  true)
                                        ? getTranslation("mooselikeHuntingFinished")
                                        : localiser.getTranslation(item.getHarvestReportState()))
                .appendTextCell(localiser.getTranslation(item.getRka()))
                .appendTextCell(localiser.getTranslation(item.getRhy()))
        );

        helper.autoSizeColumns();
    }

    private void createImmaterialPermitSheet(final Workbook workbook) {
        final String sheetName = getTranslation("permitImmaterialSheetName");
        final ExcelHelper helper = new ExcelHelper(workbook, sheetName);
        final List<String> headerRow = Arrays.asList(
                "permitNumber",
                "permitType",
                "permitDates",
                "rka");

        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, Iterables.toArray(headerRow, String.class)));
        immaterialPermitData.forEach(item -> helper.appendRow()
                .appendTextCell(item.getPermitNumber())
                .appendTextCell(localiser.getTranslation(item.getPermitType()))
                .appendTextCell(item.getPermitDates())
                .appendTextCell(localiser.getTranslation(item.getRka())));

        helper.autoSizeColumns();
    }
}
