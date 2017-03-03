package fi.riista.feature.account.certificate;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.JCEUtil;
import fi.riista.util.LocalisedString;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.security.PrivateKey;
import java.util.Objects;

public class HuntingCardQRCodeGenerator {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("ddMMyyyy");

    private static final char FIELD_SEPARATOR = ';';
    private static final Joiner FIELD_JOINER = Joiner.on(FIELD_SEPARATOR).useForNull("");

    public static HuntingCardQRCodeGenerator forPerson(Person person) {
        return new HuntingCardQRCodeGenerator()
                .withLastName(person.getLastName())
                .withFirstNames(person.getFirstName())
                .withHomeMunicipalityName(person.getHomeMunicipalityName())
                .withDateOfBirth(person.parseDateOfBirth())
                .withHunterNumber(person.getHunterNumber())
                .withHuntingCardEnd(person.getHuntingCardEnd())
                .withRhyOfficialCode(person.getRhyMembership());
    }

    private String lastName;
    private String firstNames;
    private LocalisedString homeMunicipalityName;
    private LocalDate dateOfBirth;
    private String hunterNumber;
    private LocalDate huntingCardEnd;
    private String rhyOfficialCode;

    public HuntingCardQRCodeGenerator withLastName(final String value) {
        this.lastName = StringUtils.trimToNull(value);
        return this;
    }

    public HuntingCardQRCodeGenerator withFirstNames(final String value) {
        this.firstNames = StringUtils.trimToNull(value);
        return this;
    }

    public HuntingCardQRCodeGenerator withHomeMunicipalityName(final LocalisedString value) {
        if (value == null) {
            this.homeMunicipalityName = LocalisedString.EMPTY;
        } else {
            this.homeMunicipalityName = value;
        }
        return this;
    }

    public HuntingCardQRCodeGenerator withDateOfBirth(final LocalDate value) {
        this.dateOfBirth = value;
        return this;
    }

    public HuntingCardQRCodeGenerator withHunterNumber(final String value) {
        this.hunterNumber = value;
        return this;
    }

    public HuntingCardQRCodeGenerator withHuntingCardEnd(final LocalDate value) {
        this.huntingCardEnd = value;
        return this;
    }

    public HuntingCardQRCodeGenerator withRhyOfficialCode(final Riistanhoitoyhdistys rhy) {
        this.rhyOfficialCode = rhy != null ? rhy.getOfficialCode() : null;
        return this;
    }

    public String buildWithoutSignature(final String language) {
        Objects.requireNonNull(lastName);
        Objects.requireNonNull(firstNames);
        Objects.requireNonNull(homeMunicipalityName);
        Objects.requireNonNull(dateOfBirth);
        Objects.requireNonNull(hunterNumber);
        Objects.requireNonNull(huntingCardEnd);
        Objects.requireNonNull(rhyOfficialCode);

        // Max 98 characters
        final String[] parts = {
                // 25 characters
                limitLength(25, lastName),
                // 27 characters
                limitLength(27, firstNames),
                // 18 characters
                limitLength(18, homeMunicipalityName.getAnyTranslation(language)),
                // 8 characters
                DATE_FORMAT.print(dateOfBirth),
                // 8 characters
                hunterNumber,
                // 8 characters
                DATE_FORMAT.print(huntingCardEnd),
                // 3 characters
                rhyOfficialCode
        };

        return FIELD_JOINER.join(parts);
    }

    private static String limitLength(int maxLength, String data) {
        Objects.requireNonNull(data);

        if (maxLength <= 0) {
            throw new IllegalArgumentException("Invalid maxLength");
        }

        return data.substring(0, Math.min(data.length(), maxLength));
    }

    private static String createSignature(final PrivateKey privateKey, final String payload) {
        try {
            return JCEUtil.createECDSASignature(privateKey, payload);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public String build(final PrivateKey privateKey, final String language) {
        // Max 98 characters
        final String payload = buildWithoutSignature(language);
        // Max 53 characters
        final String signature = createSignature(privateKey, payload);

        // Total max 152 characters
        return payload + FIELD_SEPARATOR + signature;
    }
}
