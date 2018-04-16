package fi.riista.feature.organization.occupation;

import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.pub.occupation.PublicOccupationDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.Localiser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class OccupationExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormat.forPattern("d.M.yyyy");
    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    public static final Predicate<OccupationDTO> PAST = occ -> {
        Objects.requireNonNull(occ, "occ must not be null");
        return occ.isEndDateBefore(DateUtil.today());
    };

    public static final Predicate<OccupationDTO> FUTURE = occ -> {
        Objects.requireNonNull(occ, "occ must not be null");
        return occ.isBeginDateAfter(DateUtil.today());
    };

    public static final Predicate<OccupationDTO> CURRENT = occ -> {
        Objects.requireNonNull(occ, "occ must not be null");
        final LocalDate today = DateUtil.today();

        return (occ.getBeginDate() == null || occ.getBeginDate().compareTo(today) < 0)
                && (occ.getEndDate() == null || occ.getEndDate().compareTo(today) > 0);
    };

    private static final String[] HEADERS_FI = {
            "Organisaation nimi",
            "Tehtävän nimi",
            "Aloituspäivä",
            "Lopetuspäivä",
            "Sukunimi",
            "Etunimi",
            "Sähköpostiosoite",
            "Puhelinnumero",
            "Katuosoite",
            "Postinumero",
            "Kaupunki",
            "Maa"
    };

    private static final String[] HEADERS_SV = {
            "Organisation",
            "Uppdrag",
            "Startdatum",
            "Slutdatum",
            "Efternamn",
            "Förnamn",
            "E-Postadress",
            "Telefonnummer",
            "Gatuadress",
            "Postnummer",
            "Stad",
            "Land"
    };

    private final Locale locale;
    private final MessageSource messageSource;
    private final String organisationName;
    private final OrganisationType organisationType;
    private final List<OccupationDTO> occupations;

    public OccupationExcelView(Locale locale, MessageSource messageSource, String organisationName, OrganisationType organisationType, List<OccupationDTO> occupations) {
        this.locale = locale;
        this.messageSource = messageSource;
        this.organisationName = organisationName;
        this.organisationType = organisationType;
        this.occupations = occupations;
    }

    private String[] createRow(OccupationDTO result) {
        final String occupationType = messageSource.getMessage(PublicOccupationDTO.class.getSimpleName() + "."
                + organisationType.name()
                + "."
                + result.getOccupationType().name(), null, locale);
        final String begin = printDate(result.getBeginDate());
        final String end = printDate(result.getEndDate());

        final AddressDTO addressDTO = getAddressDto(result);
        return new String[] {
                organisationName,
                occupationType,
                begin,
                end,
                result.getPerson().getLastName(),
                result.getPerson().getFirstName(),
                result.getPerson().getEmail(),
                result.getPerson().getPhoneNumber(),
                addressDTO.getStreetAddress(),
                addressDTO.getPostalCode(),
                addressDTO.getCity(),
                addressDTO.getCountry()
        };
    }

    private static AddressDTO getAddressDto(OccupationDTO result) {
        return result.getPerson().getAddress() != null
                ? result.getPerson().getAddress()
                : new AddressDTO();
    }

    private static String printDate(LocalDate date) {
        if (date == null) {
            return Localiser.select("toistaiseksi", "tills vidare");
        }
        return DATE_PATTERN.print(date);
    }

    private String createFilename() {
        final String timestamp = DATETIME_PATTERN.print(DateUtil.now());
        final String prefix = messageSource.getMessage("occupations.title", null, locale);
        return prefix + "-" + timestamp + ".xls";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename=" + createFilename());

        createSheet(workbook, "occupations.current", F.filterToList(occupations, CURRENT));
        createSheet(workbook, "occupations.future", F.filterToList(occupations, FUTURE));
        createSheet(workbook, "occupations.past", F.filterToList(occupations, PAST));
    }

    private void createSheet(Workbook workbook, String sheetNameKey, List<OccupationDTO> sheetOccupations) {
        Sheet sheet = workbook.createSheet(messageSource.getMessage(sheetNameKey, null, locale));
        sheet.setDefaultColumnWidth(20);

        createHeaderRow(sheet);

        int row = 2;
        for (OccupationDTO result : sheetOccupations) {
            Row sheetRow = sheet.createRow(row++);

            int col = 0;
            for (String cellValue : createRow(result)) {
                sheetRow.createCell(col++).setCellValue(cellValue);
            }
        }
    }

    private void createHeaderRow(Sheet sheet) {
        String[] headersNames = Locales.isSwedish(locale) ? HEADERS_SV : HEADERS_FI;

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headersNames.length; i++) {
            headerRow.createCell(i).setCellValue(headersNames[i]);
        }
    }
}
