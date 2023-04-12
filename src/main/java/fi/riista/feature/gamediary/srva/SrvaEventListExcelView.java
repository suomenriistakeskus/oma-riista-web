package fi.riista.feature.gamediary.srva;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class SrvaEventListExcelView extends AbstractXlsxView {

    private static final String[] HEADER_LOCALIZATION_KEYS = new String[]{
            "SrvaEventExportExcel.srvaEventId",
            "state",
            "date",
            "clockTime",
            "SrvaEventExportExcel.event",
            "SrvaEventExportExcel.deportationOrderNumber",
            "SrvaEventExportExcel.type",
            "SrvaEventExportExcel.otherType",
            "SrvaEventExportExcel.typeDetail",
            "SrvaEventExportExcel.otherTypeDetail",
            "SrvaEventExportExcel.description",
            "species",
            "SrvaEventExportExcel.otherSpecies",
            "SrvaEventExportExcel.numberOfAnimals",
            "gender",
            "age",
            "SrvaEventExportExcel.result",
            "SrvaEventExportExcel.resultDetail",
            "SrvaEventExportExcel.methods",
            "SrvaEventExportExcel.otherMethod",
            "SrvaEventExportExcel.personCount",
            "SrvaEventExportExcel.timeSpent",
            "rhyAbbrv",
            "geolocationSource",
            "geolocationAccuracy",
            "latitude",
            "longitude",
            "SrvaEventExportExcel.fromMobile",
            "lastNameOfAuthor",
            "firstNameOfAuthor",
            "phoneNumberOfAuthor",
            "emailOfAuthor"
    };

    private final List<SrvaEventExportExcelDTO> data;

    private final EnumLocaliser localiser;

    public SrvaEventListExcelView(final List<SrvaEventExportExcelDTO> data, final EnumLocaliser enumLocaliser) {
        this.data = data;
        this.localiser = enumLocaliser;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper excelHelper = new ExcelHelper(workbook).appendHeaderRow(getRowHeaders());

        for (final SrvaEventExportExcelDTO dto : data) {
            excelHelper
                    .appendRow()
                    .appendNumberCell(dto.getSrvaEventId())
                    .appendTextCell(dto.getState())
                    .appendDateCell(DateUtil.toDateNullSafe(dto.getDate()))
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(dto.getTime()))
                    .appendTextCell(dto.getEventName())
                    .appendTextCell(dto.getDeportationOrderNumber())
                    .appendTextCell(dto.getEventType())
                    .appendTextCell(dto.getOtherTypeDescription())
                    .appendTextCell(dto.getEventTypeDetail())
                    .appendTextCell(dto.getOtherTypeDetailDescription())
                    .appendTextCell(dto.getDescription())
                    .appendTextCell(dto.getAnimalSpecies())
                    .appendTextCell(dto.getOtherSpeciesDescription())
                    .appendNumberCell(dto.getSpecimenAmount())
                    .appendTextCell(dto.getSpecimenGenders())
                    .appendTextCell(dto.getSpecimenAges())
                    .appendTextCell(dto.getEventResult())
                    .appendTextCell(dto.getEventResultDetail())
                    .appendTextCell(dto.getEventMethods())
                    .appendTextCell(dto.getOtherMethodDescription())
                    .appendNumberCell(dto.getPersonCount())
                    .appendNumberCell(dto.getTimeSpent())
                    .appendTextCell(dto.getRhyName())
                    .appendTextCell(localiser.getTranslation(dto.getCoordinatesCollectionMethod()))
                    .appendNumberCell(dto.getCoordinatesAccuracy())
                    .appendNumberCell(dto.getCoordinatesLatitude())
                    .appendNumberCell(dto.getCoordinatesLongitude())
                    .appendTextCell(localiser.getTranslation(dto.isFromMobile()))
                    .appendTextCell(dto.getSubmitterLastName())
                    .appendTextCell(dto.getSubmitterFirstName())
                    .appendTextCell(dto.getSubmitterPhone())
                    .appendTextCell(dto.getSubmitterEmail());
        }

        excelHelper.autoSizeColumns();
    }

    private String[] getRowHeaders() {
        return localiser.translate(HEADER_LOCALIZATION_KEYS);
    }

    private String createFilename() {
        return String.format(
                "%s-%s.xlsx",
                StringUtils.uncapitalize(localiser.getTranslation("SrvaEventExportExcel.srvaEvents")),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }
}
