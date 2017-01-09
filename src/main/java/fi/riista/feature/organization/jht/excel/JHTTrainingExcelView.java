package fi.riista.feature.organization.jht.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.jht.training.JHTTrainingDTO;
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

public class JHTTrainingExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

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
    private final MessageSource messageSource;
    private final Locale locale;

    public JHTTrainingExcelView(final List<JHTTrainingDTO> results,
                                final MessageSource messageSource,
                                final Locale locale) {
        this.results = results;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    private static String createFilename() {
        final String timestamp = DATETIME_PATTERN.print(DateUtil.now());
        return "koulutukset-" + timestamp + ".xls";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, LocaleContextHolder.getLocale());
        final ExcelHelper helper = new ExcelHelper(workbook, "koulutukset");

        response.setHeader(ContentDispositionUtil.HEADER_NAME,
                ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        helper.appendHeaderRow(Locales.isSwedish(locale) ? HEADERS_SV : HEADERS_FI);

        for (final JHTTrainingDTO result : this.results) {
            helper.appendRow()
                    .appendTextCell(enumLocaliser.getTranslation(result.getOccupationType()))
                    .appendTextCell(enumLocaliser.getTranslation(result.getTrainingType()))
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
