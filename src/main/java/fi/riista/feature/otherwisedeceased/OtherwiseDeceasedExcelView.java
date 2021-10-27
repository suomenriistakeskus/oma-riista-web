package fi.riista.feature.otherwisedeceased;

import com.google.common.collect.Iterables;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.now;

public class OtherwiseDeceasedExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "OtherwiseDeceasedExcel.";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("d.M.yyyy HH:mm");


    private final EnumLocaliser localiser;
    private final List<OtherwiseDeceasedDTO> data;
    private final Map<Integer, LocalisedString> speciesNameMap;


    public OtherwiseDeceasedExcelView(final EnumLocaliser localiser,
                                      final List<OtherwiseDeceasedDTO> data,
                                      final Map<Integer, LocalisedString> speciesNameMap) {
        this.localiser = localiser;
        this.data = data;
        this.speciesNameMap = speciesNameMap;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook);
    }

    private String createFilename() {
        return String.format(
                "%s_%s.xlsx",
                getTranslation("filename"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private String getTranslation(final String localisationKey) {
        return localiser.getTranslation(LOCALISATION_PREFIX + localisationKey);
    }

    /* package */ void createSheet(final Workbook workbook) {
        final String sheetName = getTranslation("sheetName");
        final ExcelHelper helper = new ExcelHelper(workbook, sheetName);
        final List<String> header = Arrays.asList(
                "pointOfTime",
                "species",
                "age",
                "gender",
                "weight",
                "rejected",
                "cause",
                "causeOther",
                "source",
                "sourceOther",
                "rka",
                "rhy",
                "municipality",
                "noExactLocation",
                "longitude",
                "latitude",
                "attachments",
                "description",
                "additionalInfo",
                "creationTime",
                "createdBy",
                "modificationTime",
                "modifiedBy"
        );
        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, Iterables.toArray(header, String.class)));

        data.forEach(item -> helper.appendRow()
                .appendTextCell(item.getPointOfTime().toString(DATETIME_FORMATTER))
                .appendTextCell(localiser.getTranslation(speciesNameMap.get(item.getGameSpeciesCode())))
                .appendTextCell(localiser.getTranslation(item.getAge()))
                .appendTextCell(localiser.getTranslation(item.getGender()))
                .appendNumberCell(item.getWeight())
                .appendTextCell(localiser.getTranslation(item.isRejected()))
                .appendTextCell(localiser.getTranslation(item.getCause()))
                .appendTextCell(item.getCauseOther())
                .appendTextCell(localiser.getTranslation(item.getSource()))
                .appendTextCell(item.getSourceOther())
                .appendTextCell(localiser.getTranslation(item.getRka().getNameLocalisation()))
                .appendTextCell(localiser.getTranslation(item.getRhy().getNameLocalisation()))
                .appendTextCell(localiser.getTranslation(item.getMunicipality().getNameLocalisation()))
                .appendTextCell(localiser.getTranslation(!item.isNoExactLocation())) // Note! Negation.
                .appendNumberCell(item.getGeoLocation().getLongitude())
                .appendNumberCell(item.getGeoLocation().getLatitude())
                .appendNumberCell(item.getAttachments().size())
                .appendWrappedTextCell(item.getDescription())
                .appendWrappedTextCell(item.getAdditionalInfo())
                .appendTextCell(getCreationTime(item))
                .appendTextCell(getCreatedBy(item))
                .appendTextCell(getModificationTIme(item))
                .appendTextCell(getModifiedBy(item))
        );

        helper.autoSizeColumns();

        final int columnWidth = 50 * 256; // width: 1/256 of char width
        helper.setColumnWidth(header.indexOf("description"), columnWidth);
        helper.setColumnWidth(header.indexOf("additionalInfo"), columnWidth);
    }

    private String getCreationTime(final OtherwiseDeceasedDTO item) {
        return item.getChangeHistory().get(0).getModificationTime().toString(DATETIME_FORMATTER);
    }

    private String getCreatedBy(final OtherwiseDeceasedDTO item) {
        return String.format("%s %s",
                             item.getChangeHistory().get(0).getAuthor().getFirstName(),
                             item.getChangeHistory().get(0).getAuthor().getLastName());
    }

    private String getModificationTIme(final OtherwiseDeceasedDTO item) {
        if (item.getChangeHistory().size() == 1) {
            return "";
        }
        return Iterables.getLast(item.getChangeHistory()).getModificationTime().toString(DATETIME_FORMATTER);
    }

    private String getModifiedBy(final OtherwiseDeceasedDTO item) {
        if (item.getChangeHistory().size() == 1) {
            return "";
        }
        return String.format("%s %s",
                             Iterables.getLast(item.getChangeHistory()).getAuthor().getFirstName(),
                             Iterables.getLast(item.getChangeHistory()).getAuthor().getLastName());
    }
}
