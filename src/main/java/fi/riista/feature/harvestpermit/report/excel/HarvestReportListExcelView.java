package fi.riista.feature.harvestpermit.report.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
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

public class HarvestReportListExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String[] HEADER_LOCALIZATION_KEYS = new String[] {
            "harvestReportId",
            "state",
            "date",
            "clockTime",

            "species",
            "gender",
            "age",
            "weight",

            "permitNumber",
            "permitType",
            "harvestQuotaArea",
            "rka",
            "rhyAbbrv",

            "geolocationSource",
            "geolocationAccuracy",
            "latitude",
            "longitude",

            "harvestArea",
            "nameOfHuntingClubOrParty",
            "surfaceAreaOfRegion",
            "realEstateNumber",

            "lastNameOfAuthor",
            "firstNameOfAuthor",
            "addressOfAuthor",
            "postalCodeOfAuthor",
            "postOfficeOfAuthor",
            "phoneNumberOfAuthor",
            "emailOfAuthor",

            "lastNameOfHunter",
            "firstNameOfHunter",
            "addressOfHunter",
            "postalCodeOfHunter",
            "postOfficeOfHunter",
            "phoneNumberOfHunter",
            "emailOfHunter",
            "huntingCardOfHunter",

            "reportingTime"
    };

    private final List<HarvestReportExportExcelDTO> data;
    private final EnumLocaliser localiser;

    public HarvestReportListExcelView(
            final List<HarvestReportExportExcelDTO> data, final EnumLocaliser enumLocaliser) {

        this.data = data;
        this.localiser = enumLocaliser;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        response.setHeader(ContentDispositionUtil.HEADER_NAME,
                ContentDispositionUtil.encodeAttachmentFilename(createFilename()));
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);

        final ExcelHelper excelHelper = new ExcelHelper(workbook).appendHeaderRow(getRowHeaders());

        for (final HarvestReportExportExcelDTO dto : data) {
            excelHelper
                    .appendRow()
                    .appendNumberCell(dto.getSubmissionId())
                    .appendTextCell(dto.getState())
                    .appendDateCell(DateUtil.toDateNullSafe(dto.getDateOfCatch()))
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(dto.getTimeOfCatch()))

                    .appendTextCell(dto.getAnimalSpecies())
                    .appendTextCell(dto.getGenderName())
                    .appendTextCell(dto.getAgeName())
                    .appendTextCell(dto.getWeight())

                    .appendTextCell(dto.getHuntingLicenseNumber())
                    .appendTextCell(dto.getHuntingLicenseType())
                    .appendTextCell(dto.getQuotaAreaName())
                    .appendTextCell(dto.getRkkAreaName())
                    .appendTextCell(dto.getRhyName())

                    .appendTextCell(localiser.getTranslation(dto.getCoordinatesCollectionMethod()))
                    .appendNumberCell(dto.getCoordinatesAccuracy())
                    .appendNumberCell(dto.getCoordinatesLatitude())
                    .appendNumberCell(dto.getCoordinatesLongitude())

                    .appendTextCell(dto.getHuntingArea() == HuntingAreaType.HUNTING_SOCIETY ? "S" : "T")
                    .appendTextCell(dto.getHuntingGroup())
                    .appendNumberCell(dto.getArea())
                    .appendTextCell(dto.getPropertyIdentifier())

                    .appendTextCell(dto.getSubmitterLastName())
                    .appendTextCell(dto.getSubmitterFirstName())
                    .appendTextCell(dto.getSubmitterAddress())
                    .appendTextCell(dto.getSubmitterPostalCode())
                    .appendTextCell(dto.getSubmitterPostalResidence())
                    .appendTextCell(dto.getSubmitterPhone())
                    .appendTextCell(dto.getSubmitterEmail())

                    .appendTextCell(dto.getHunterLastName())
                    .appendTextCell(dto.getHunterFirstName())
                    .appendTextCell(dto.getHunterAddress())
                    .appendTextCell(dto.getHunterPostalCode())
                    .appendTextCell(dto.getHunterPostalResidence())
                    .appendTextCell(dto.getHunterPhone())
                    .appendTextCell(dto.getHunterEmail())
                    .appendTextCell(dto.getHunterHuntingCard())

                    .appendDateTimeCell(dto.getReportingTime());
        }
    }

    private String[] getRowHeaders() {
        final List<String> headers = F.mapNonNullsToList(HEADER_LOCALIZATION_KEYS, localiser.asFunction());
        return headers.toArray(new String[headers.size()]);
    }

    private String createFilename() {
        return String.format(
                "%s-%s.xls",
                StringUtils.uncapitalize(localiser.getTranslation("harvestReports")),
                DATETIME_PATTERN.print(DateUtil.now()));
    }

}
