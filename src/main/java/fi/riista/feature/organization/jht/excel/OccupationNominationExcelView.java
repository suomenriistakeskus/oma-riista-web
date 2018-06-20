package fi.riista.feature.organization.jht.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.jht.nomination.OccupationNominationDTO;
import fi.riista.feature.organization.jht.nomination.OccupationNominationSearchDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Locales;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OccupationNominationExcelView extends AbstractXlsxView {

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
    private final OccupationNominationSearchDTO searchDTO;
    private final EnumLocaliser localiser;
    private final boolean isSwedish;

    public OccupationNominationExcelView(final List<OccupationNominationDTO> results,
                                         final OccupationNominationSearchDTO dto,
                                         final MessageSource messageSource,
                                         final Locale locale) {
        this.results = results;
        this.searchDTO = dto;
        this.localiser = new EnumLocaliser(messageSource, locale);
        this.isSwedish = Locales.isSwedish(locale);
    }

    private static String createFilename(final String sheetName) {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());

        return sheetName.toLowerCase() + "-" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        final String sheetName = localiser.getTranslation(searchDTO.getNominationStatus());

        ContentDispositionUtil.addHeader(response, createFilename(sheetName));

        final ExcelHelper helper = new ExcelHelper(workbook, sheetName)
                .appendHeaderRow(this.isSwedish ? HEADERS_SV : HEADERS_FI);

        for (final OccupationNominationDTO result : this.results) {
            helper.appendRow()
                    .appendTextCell(result.getRhy().getOfficialCode())
                    .appendTextCell(result.getRhy().getNameFI())
                    .appendTextCell(localiser.getTranslation(result.getOccupationType()))
                    .appendTextCell(localiser.getTranslation(result.getNominationStatus()))
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
