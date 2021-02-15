package fi.riista.feature.harvestpermit.report.excel;

import fi.riista.config.Constants;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Localiser;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HarvestReportListExcelView extends AbstractXlsxView {
    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    public static HarvestReportListExcelView create(final Localiser localiser,
                                                    final List<HarvestReportExcelDTO> data,
                                                    final boolean includeDetails) {
        final String[] rowHeaders = localiser.translate(includeDetails
                ? HEADER_LOCALIZATION_KEYS_DETAILS
                : HEADER_LOCALIZATION_KEYS);
        final String filename = String.format("%s-%s.xlsx",
                StringUtils.uncapitalize(localiser.getTranslation("harvestReports")),
                DATETIME_PATTERN.print(DateUtil.now()));

        return new HarvestReportListExcelView(data, rowHeaders, filename, includeDetails);
    }

    /*package*/ static final String[] HEADER_LOCALIZATION_KEYS_DETAILS = new String[]{
            "state", "reportingTime", "date", "clockTime",
            "species", "gender", "age", "weight",
            "rka", "rhyAbbrv",
            "permitNumber", "permitType",
            "harvestSeasonName", "harvestQuotaArea",
            "feedingPlace", "taigaBeanGoose", "reportedWithPhoneCall", "huntingMethod", "huntingAreaType",
            "nameOfHuntingClubOrParty", "huntingAreaSize",
            "geolocationSource", "geolocationAccuracy", "latitude", "longitude", "propertyIdentifier", "municipality",

            "lastNameOfAuthor", "firstNameOfAuthor", "addressOfAuthor", "postalCodeOfAuthor", "postOfficeOfAuthor",
            "phoneNumberOfAuthor", "emailOfAuthor",
            "lastNameOfHunter", "firstNameOfHunter", "addressOfHunter", "postalCodeOfHunter", "postOfficeOfHunter",
            "phoneNumberOfHunter", "emailOfHunter", "huntingCardOfHunter"
    };

    /*package*/ static final String[] HEADER_LOCALIZATION_KEYS = new String[]{
            "state", "reportingTime", "date", "clockTime",
            "species", "gender", "age", "weight",
            "rka", "rhyAbbrv",
            "permitNumber", "permitType",
            "harvestSeasonName", "harvestQuotaArea",
            "feedingPlace", "taigaBeanGoose", "reportedWithPhoneCall", "huntingMethod", "huntingAreaType",
            "nameOfHuntingClubOrParty", "huntingAreaSize",
            "geolocationSource", "geolocationAccuracy", "latitude", "longitude", "propertyIdentifier", "municipality"
    };

    private final List<HarvestReportExcelDTO> data;
    private final String[] rowHeaders;
    private final String filename;
    private final boolean includeDetails;

    /*package*/ void buildForTest(final Workbook workbook, final HttpServletResponse response) {
        buildExcelDocument(null, workbook, null, response);
    }

    protected HarvestReportListExcelView(final List<HarvestReportExcelDTO> data,
                                       final String[] rowHeaders,
                                       final String filename,
                                       final boolean includeDetails) {
        this.data = Objects.requireNonNull(data);
        this.rowHeaders = Objects.requireNonNull(rowHeaders);
        this.filename = Objects.requireNonNull(filename);
        this.includeDetails = includeDetails;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        final ExcelHelper excelHelper = new ExcelHelper(workbook).appendHeaderRow(rowHeaders);

        for (final HarvestReportExcelDTO dto : data) {
            excelHelper
                    .appendRow()
                    .appendTextCell(dto.getHarvestReportState())
                    .appendDateTimeCell(dto.getHarvestReportDate())
                    .appendDateCell(dto.getPointOfTime())
                    .appendTimeCell(dto.getPointOfTime())

                    // specimen fields

                    .appendTextCell(dto.getSpeciesName())
                    .appendTextCell(dto.getGenderName())
                    .appendTextCell(dto.getAgeName())
                    .appendTextCell(dto.getWeight())

                    // rhy + rka

                    .appendTextCell(dto.getRkaName())
                    .appendTextCell(dto.getRhyName())

                    // permit fields

                    .appendTextCell(dto.getPermitNumber())
                    .appendTextCell(dto.getPermitType())

                    // season fields

                    .appendTextCell(dto.getSeasonName())
                    .appendTextCell(dto.getQuotaAreaName())
                    .appendTextCell(dto.getFeedingPlace())
                    .appendTextCell(dto.getTaigaBeanGoose())
                    .appendTextCell(dto.getReportedWithPhoneCall())
                    .appendTextCell(dto.getHuntingMethodName())
                    .appendTextCell(dto.getHuntingAreaType())
                    .appendTextCell(dto.getHuntingGroupName())
                    .appendNumberCell(dto.getHuntingAreaSize())

                    // location + gis

                    .appendTextCell(dto.getLocationSourceName())
                    .appendNumberCell(dto.getLocationAccuracy())
                    .appendNumberCell(dto.getLocationLatitude())
                    .appendNumberCell(dto.getLocationLongitude())
                    .appendTextCell(dto.getPropertyIdentifier())
                    .appendTextCell(dto.getMunicipalityCode());

            if (includeDetails) {
                excelHelper

                        // author
                        .appendTextCell(dto.getHarvestReportAuthorLastName())
                        .appendTextCell(dto.getHarvestReportAuthorFirstName())
                        .appendTextCell(dto.getHarvestReportAuthorAddress())
                        .appendTextCell(dto.getHarvestReportAuthorPostalCode())
                        .appendTextCell(dto.getHarvestReportAuthorPostalResidence())
                        .appendTextCell(dto.getHarvestReportAuthorPhone())
                        .appendTextCell(dto.getHarvestReportAuthorEmail())

                        // actor
                        .appendTextCell(dto.getHunterLastName())
                        .appendTextCell(dto.getHunterFirstName())
                        .appendTextCell(dto.getHunterAddress())
                        .appendTextCell(dto.getHunterPostalCode())
                        .appendTextCell(dto.getHunterPostalResidence())
                        .appendTextCell(dto.getHunterPhone())
                        .appendTextCell(dto.getHunterEmail())
                        .appendTextCell(dto.getHunterHuntingCard());

            }
        }
    }
}
