package fi.riista.feature.organization.jht.expiry;

import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class JHTOccupationExpiryDTO {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy");

    private final Locale locale;
    private final LocalDate expiryDate;
    private final String personName;
    private final LocalisedString occupationName;
    private final String occupationEmail;
    private final long rhyId;
    private final LocalisedString rhyName;

    public JHTOccupationExpiryDTO(final @Nonnull Locale locale,
                                  final @Nonnull LocalDate expiryDate,
                                  final @Nonnull String personName,
                                  final @Nonnull LocalisedString occupationName,
                                  final @Nonnull String occupationEmail,
                                  final @Nonnull Long rhyId,
                                  final @Nonnull LocalisedString rhyName) {
        this.locale = requireNonNull(locale);
        this.expiryDate = requireNonNull(expiryDate);
        this.personName = requireNonNull(personName);
        this.occupationName = requireNonNull(occupationName);
        this.occupationEmail = requireNonNull(occupationEmail);
        this.rhyId = requireNonNull(rhyId);
        this.rhyName = requireNonNull(rhyName);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getExpiryDate() {
        return DTF.print(expiryDate);
    }

    public String getPersonName() {
        return personName;
    }

    public String getOccupationName() {
        return occupationName.getAnyTranslation(locale);
    }

    public String getOccupationEmail() {
        return occupationEmail;
    }

    public long getRhyId() {
        return rhyId;
    }

    public String getRhyName() {
        return rhyName.getAnyTranslation(locale);
    }

    public String getRhyReceiverName() {
        return getRhyName().replaceAll("riistanhoitoyhdistys", "riistanhoitoyhdistykselle");
    }
}
