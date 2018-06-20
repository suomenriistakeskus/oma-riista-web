package fi.riista.feature.organization.jht.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.jht.training.JHTTrainingDTO;
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

public class JHTTrainingExcelView extends AbstractXlsxView {

    private static final String[] HEADERS_FI = {
            "Tehtävä",
            "Koulutustyyppi",
            "Koulutuspäivä",
            "Koulutuspaikka",
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
            "Uppdrag",
            "Koulutustyyppi",
            "Koulutuspäivä",
            "Koulutuspaikka",
            "Efternamn",
            "Förnamn",
            "E-Postadress",
            "Telefonnummer",
            "Gatuadress",
            "Postnummer",
            "Stad",
            "Land"
    };

    private final List<JHTTrainingDTO> results;
    private final EnumLocaliser localiser;
    private final boolean isSwedish;

    public JHTTrainingExcelView(final List<JHTTrainingDTO> results,
                                final MessageSource messageSource,
                                final Locale locale) {

        this.results = results;
        this.localiser = new EnumLocaliser(messageSource, locale);
        this.isSwedish = Locales.isSwedish(locale);
    }

    private static String createFilename() {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        return "koulutukset-" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook, "koulutukset")
                .appendHeaderRow(this.isSwedish ? HEADERS_SV : HEADERS_FI);

        for (final JHTTrainingDTO result : this.results) {
            helper.appendRow()
                    .appendTextCell(localiser.getTranslation(result.getOccupationType()))
                    .appendTextCell(localiser.getTranslation(result.getTrainingType()))
                    .appendDateCell(result.getTrainingDate())
                    .appendTextCell(result.getTrainingLocation());

            final JHTTrainingDTO.PersonDTO person = result.getPerson();

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
