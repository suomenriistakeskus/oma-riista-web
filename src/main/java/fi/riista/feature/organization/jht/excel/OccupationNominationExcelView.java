package fi.riista.feature.organization.jht.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.jht.nomination.OccupationNominationDTO;
import fi.riista.feature.organization.jht.nomination.OccupationNominationSearchDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Locales;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OccupationNominationExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String[] HEADERS_FI = {
            "RHY-koodi",
            "RHY",
            "Tehtävä",
            "Status",
            "Nimityspäivä",
            "Päätöspäivä",
            "Käsittelijä",
            "Tehtävän alkupvm.",
            "Tehtävän loppupvm.",
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
            "JVF-nummer",
            "JVF",
            "Uppdrag",
            "Status",
            "Nimityspäivä",
            "Päätöspäivä",
            "Käsittelijä",
            "Tehtävän alkupvm.",
            "Tehtävän loppupvm.",
            "Efternamn",
            "Förnamn",
            "E-Postadress",
            "Telefonnummer",
            "Gatuadress",
            "Postnummer",
            "Stad",
            "Land"
    };

    private final List<OccupationNominationDTO> results;
    private final MessageSource messageSource;
    private final Locale locale;
    private final OccupationNominationSearchDTO searchDTO;

    public OccupationNominationExcelView(final List<OccupationNominationDTO> results,
                                         final OccupationNominationSearchDTO dto,
                                         final MessageSource messageSource,
                                         final Locale locale) {
        this.results = results;
        this.searchDTO = dto;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    private static String createFilename(final String sheetName) {
        final String timestamp = DATETIME_PATTERN.print(DateUtil.now());

        return sheetName.toLowerCase() + "-" + timestamp + ".xls";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, LocaleContextHolder.getLocale());
        final String sheetName = enumLocaliser.getTranslation(searchDTO.getNominationStatus());
        final ExcelHelper helper = new ExcelHelper(workbook, sheetName);

        response.setHeader(ContentDispositionUtil.HEADER_NAME,
                ContentDispositionUtil.encodeAttachmentFilename(createFilename(sheetName)));

        helper.appendHeaderRow(Locales.isSwedish(locale) ? HEADERS_SV : HEADERS_FI);

        for (final OccupationNominationDTO result : this.results) {
            helper.appendRow()
                    .appendTextCell(result.getRhy().getOfficialCode())
                    .appendTextCell(result.getRhy().getNameFI())
                    .appendTextCell(enumLocaliser.getTranslation(result.getOccupationType()))
                    .appendTextCell(enumLocaliser.getTranslation(result.getNominationStatus()))
                    .appendDateCell(result.getNominationDate())
                    .appendDateCell(result.getDecisionDate())
                    .appendTextCell(result.getModeratorFullName());

            if (result.getOccupationPeriod() != null) {
                helper.appendDateCell(result.getOccupationPeriod().getBeginDate());
                helper.appendDateCell(result.getOccupationPeriod().getEndDate());
            } else {
                helper.appendEmptyCell(2);
            }

            final OccupationNominationDTO.PersonDTO person = result.getPerson();

            if (person != null) {
                helper.appendTextCell(person.getLastName())
                        .appendTextCell(person.getFirstName())
                        .appendTextCell(person.getEmail())
                        .appendTextCell(person.getPhoneNumber());

                final AddressDTO address = person.getAddress();

                if (address != null) {
                    helper.appendTextCell(address.getStreetAddress())
                            .appendTextCell(address.getPostalCode())
                            .appendTextCell(address.getCity())
                            .appendTextCell(address.getCountry());
                }
            }
        }

        helper.autoSizeColumns();
    }
}
