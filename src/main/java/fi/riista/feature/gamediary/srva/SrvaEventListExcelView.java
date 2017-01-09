package fi.riista.feature.gamediary.srva;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class SrvaEventListExcelView extends AbstractXlsView {
    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String[] HEADER_LOCALIZATION_KEYS = new String[]{
            "SrvaEventExportExcel.srvaEventId",
            "state",
            "date",
            "clockTime",
            "SrvaEventExportExcel.event",
            "SrvaEventExportExcel.type",
            "SrvaEventExportExcel.otherType",
            "SrvaEventExportExcel.description",
            "species",
            "SrvaEventExportExcel.otherSpecies",
            "SrvaEventExportExcel.numberOfAnimals",
            "gender",
            "age",
            "SrvaEventExportExcel.result",
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
            "addressOfAuthor",
            "postalCodeOfAuthor",
            "postOfficeOfAuthor",
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
        response.setHeader(ContentDispositionUtil.HEADER_NAME,
                ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        final ExcelHelper excelHelper = new ExcelHelper(workbook).appendHeaderRow(getRowHeaders());

        for (final SrvaEventExportExcelDTO dto : data) {
            excelHelper
                    .appendRow()
                    .appendNumberCell(dto.getSrvaEventId())
                    .appendTextCell(dto.getState())
                    .appendDateCell(DateUtil.toDateNullSafe(dto.getDate()))
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(dto.getTime()))
                    .appendTextCell(dto.getEventName())
                    .appendTextCell(dto.getEventType())
                    .appendTextCell(dto.getOtherTypeDescription())
                    .appendTextCell(dto.getDescription())
                    .appendTextCell(dto.getAnimalSpecies())
                    .appendTextCell(dto.getOtherSpeciesDescription())
                    .appendNumberCell(dto.getSpecimenAmount())
                    .appendTextCell(dto.getSpecimenGenders())
                    .appendTextCell(dto.getSpecimenAges())
                    .appendTextCell(dto.getEventResult())
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
                    .appendTextCell(dto.getSubmitterAddress())
                    .appendTextCell(dto.getSubmitterPostalCode())
                    .appendTextCell(dto.getSubmitterPostalResidence())
                    .appendTextCell(dto.getSubmitterPhone())
                    .appendTextCell(dto.getSubmitterEmail());
        }

        excelHelper.autoSizeColumns();
    }

    private String[] getRowHeaders() {
        final List<String> headers = F.mapNonNullsToList(HEADER_LOCALIZATION_KEYS, localiser.asFunction());
        return headers.toArray(new String[headers.size()]);
    }

    private String createFilename() {
        return String.format(
                "%s-%s.xls",
                StringUtils.uncapitalize(localiser.getTranslation("SrvaEventExportExcel.srvaEvents")),
                DATETIME_PATTERN.print(DateUtil.now()));
    }
}
