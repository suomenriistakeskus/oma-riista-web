package fi.riista.feature.permit.application.pdf;

import java.net.URL;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class HarvestPermitApplicationPdfDTO {
    private final String filename;
    private final String headerText;
    private final Locale locale;
    private final URL printingUrl;

    public HarvestPermitApplicationPdfDTO(final String filename, final String headerText,
                                          final Locale locale, final URL printingUrl) {
        this.filename = requireNonNull(filename);
        this.headerText = requireNonNull(headerText);
        this.locale = requireNonNull(locale);
        this.printingUrl = printingUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public URL getPrintingUrl() {
        return printingUrl;
    }

    public String getFilename() {
        return filename;
    }

    public String getHeaderText() {
        return headerText;
    }
}
