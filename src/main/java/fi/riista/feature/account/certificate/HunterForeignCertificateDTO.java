package fi.riista.feature.account.certificate;

import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import java.util.Objects;

public class HunterForeignCertificateDTO {
    public static HunterForeignCertificateDTO create(final Person person, final LocalDate paymentDate) {
        Objects.requireNonNull(person);
        Objects.requireNonNull(paymentDate);

        if (person.getHunterNumber() == null) {
            throw new IllegalArgumentException("Certificate can only be generated for persons with hunterNumber");
        }

        final HunterForeignCertificateDTO model = new HunterForeignCertificateDTO();

        model.firstName = Objects.requireNonNull(person.getFirstName());
        model.lastName = Objects.requireNonNull(person.getLastName());
        model.dateOfBirth = Objects.requireNonNull(person.parseDateOfBirth());
        model.hunterNumber = person.getHunterNumber();

        model.paymentDate = paymentDate;
        model.huntingCardStart = person.getHuntingCardStart();
        model.huntingCardEnd = person.getHuntingCardEnd();

        return model;
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @FinnishHunterNumber
    private String hunterNumber;

    private LocalDate currentDate = DateUtil.today();

    private LocalDate dateOfBirth;

    private LocalDate huntingCardStart;

    private LocalDate huntingCardEnd;

    private LocalDate paymentDate;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getHuntingCardStart() {
        return huntingCardStart;
    }

    public LocalDate getHuntingCardEnd() {
        return huntingCardEnd;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
}
