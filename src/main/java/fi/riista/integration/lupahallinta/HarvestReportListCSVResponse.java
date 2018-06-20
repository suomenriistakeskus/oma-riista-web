package fi.riista.integration.lupahallinta;

import fi.riista.config.web.CSVHttpResponse;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class HarvestReportListCSVResponse extends CSVHttpResponse {
    private static final String EMPTY_VALUE = "";

    private static final String[] ROW_HEADERS = {
            "harvest_license_number", "submission_row_count", "reporting_time", "hunter_name",
            "hunter_first_name", "hunter_last_name", "hunter_address", "hunter_postal_code", "hunter_postal_residence",
            "hunter_phone", "hunter_email", "hunter_hunting_card", "harvests_as_list",
            "submitter_first_name", "submitter_last_name", "submitter_address", "submitter_postal_code",
            "submitter_postal_residence", "submitter_phone", "submitter_email",
            "rkk_area_id", "rkk_rea_name", "rhy_area_id", "rhy_area_name", "coordinates_collection_method",
            "coordinates_latitude", "coordinates_longitude", "coordinates_accuracy", "hunting_area", "hunting_group",
            "area", "municipality", "village", "property", "register_number", "date", "time", "animal_id",
            "animal_species", "seal_information", "amount", "gender_id", "gender_name", "age_id", "age_name", "weight",
            "harvest_also_reported_by_phone", "permittedMethod", "permittedMethodDescription",

            // Add extra ; to line-end just like in original
            null
    };

    private static final DateTimeFormatter DF = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TF = DateTimeFormat.forPattern("HH:mm");
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy.M.d HH:mm");
    private static final DateTimeFormatter DF_FILENAME = DateTimeFormat.forPattern("yyyyMMdd-HHmmss");

    // filename: {yyyymmdd}-{hhmmss}-riista-harvest-export-{animal-id}.csv
    public static String createFileName() {
        final String timestamp = DF_FILENAME.print(DateTime.now(DateTimeZone.forID("Europe/Helsinki")));

        return timestamp + "-riista-harvest-export.csv";
    }

    private HarvestReportListCSVResponse(String filename, String[] headerRow, List<String[]> rows) {
        super(filename, headerRow, rows);
    }

    public static HarvestReportListCSVResponse create(final Iterable<HarvestReportExportCSVDTO> rows) {
        return new HarvestReportListCSVResponse(createFileName(), ROW_HEADERS,
                F.mapNonNullsToList(rows, HarvestReportListCSVResponse::createRow));
    }

    private static String[] createRow(HarvestReportExportCSVDTO dto) {
        final String[] row = {
                dto.getHuntingLicenseNumber(),
                dto.getSubmissionRowCount() != null ? Integer.toString(dto.getSubmissionRowCount()) : null,
                dto.getReportingTime() != null ? DTF.print(dto.getReportingTime()) : null,
                dto.getHunterName(),
                dto.getHunterFirstName(),
                dto.getHunterLastName(),
                dto.getHunterAddress(),
                dto.getHunterPostalCode(),
                dto.getHunterPostalResidence(),
                dto.getHunterPhone(),
                dto.getHunterEmail(),
                dto.getHunterHuntingCard(),
                dto.getHuntingLicenseAsList(),
                dto.getSubmitterFirstName(),
                dto.getSubmitterLastName(),
                dto.getSubmitterAddress(),
                dto.getSubmitterPostalCode(),
                dto.getSubmitterPostalResidence(),
                dto.getSubmitterPhone(),
                dto.getSubmitterEmail(),
                dto.getRkkAreaId(),
                dto.getRkkAreaName(),
                dto.getRhyAreaId(),
                dto.getRhyAreaName(),
                coordinatesCollectionMethod(dto),
                formatDecimalOrNull(dto.getCoordinatesLatitude()),
                formatDecimalOrNull(dto.getCoordinatesLongitude()),
                dto.getCoordinatesAccuracy() != null ? Long.toString(Math.round(dto.getCoordinatesAccuracy())) : null,
                huntingArea(dto),
                dto.getHuntingGroup(),
                dto.getArea() != null ? Long.toString(dto.getArea()) : null,
                dto.getMunicipality(),
                dto.getVillage(),
                dto.getProperty(),
                dto.getRegisterNumber(),
                dto.getDateOfCatch() != null ? DF.print(dto.getDateOfCatch()) : null,
                dto.getTimeOfCatch() != null ? TF.print(dto.getTimeOfCatch()) : null,
                dto.getAnimalId() != null ? Integer.toString(dto.getAnimalId()) : null,
                dto.getAnimalSpecies(),
                dto.getSealInformation(),
                dto.getAmount() != null ? Integer.toString(dto.getAmount()) : null,
                dto.getGenderId(),
                dto.getGenderName(),
                dto.getAgeId(),
                dto.getAgeName(),
                dto.getWeight(),
                dto.getHarvestAlsoReportedByPhone() != null && dto.getHarvestAlsoReportedByPhone() ? "1" : "0",
                dto.getPermittedMethod(),
                dto.getPermittedMethodDescription(),
                // Add extra ; to line-end just like in original
                null
        };

        // Replace null with empty string, because that is how original export worked.
        for (int i = 0; i < row.length - 1; i++) {
            if (row[i] == null) {
                row[i] = EMPTY_VALUE;
            }
        }

        return row;
    }

    private static String huntingArea(HarvestReportExportCSVDTO dto) {
        return dto.getHuntingArea() != null
                ? dto.getHuntingArea() == HuntingAreaType.HUNTING_SOCIETY ? "S" : "T"
                : null;
    }

    private static String coordinatesCollectionMethod(HarvestReportExportCSVDTO dto) {
        return dto.getCoordinatesCollectionMethod() != null
                ? dto.getCoordinatesCollectionMethod() == GeoLocation.Source.GPS_DEVICE ? "geolocation" : "manual"
                : null;
    }

    private static String formatDecimalOrNull(Integer value) {
        return value != null ? value.toString() + ".0" : null;
    }
}
