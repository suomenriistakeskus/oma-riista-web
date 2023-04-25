package fi.riista.feature.search;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

public class SearchDTO {
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String term;
    private Locale locale;

    @Nonnull
    public String getTerm() {
        return term;
    }

    public void setTerm(@Nonnull final String term) {
        this.term = term;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }
}