package fi.riista.integration.metsastajarekisteri.person.foreign;

import fi.riista.integration.metsastajarekisteri.InnofactorConstants;
import fi.riista.integration.metsastajarekisteri.exception.IllegalAgeException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterNumberException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidPersonName;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.util.DateUtil;
import fi.riista.validation.Validators;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.regex.Pattern;

import static fi.riista.util.DateUtil.now;
import static java.lang.String.format;

public class MetsastajaRekisteriForeignPersonValidator
        implements ItemProcessor<MetsastajaRekisteriPerson, MetsastajaRekisteriPerson> {

    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriForeignPersonValidator.class);

    // Innofactor will decrement the end part starting from 9999 to avoid duplicate ssns for foreigners.
    // Allow 10 foreign persons for single date to avoid collisions with valid Finnish ssns.
    private static final Pattern FOREIGN_PERSON_SSN_PATTERN = Pattern.compile("\\d{6}(.9{3}\\d)?");

    @Override
    public MetsastajaRekisteriPerson process(final MetsastajaRekisteriPerson person) {
        if (shouldFilterOut(person)) {
            return null;
        }

        final String hunterNumber = person.getHunterNumber();

        if (!Validators.isValidHunterNumber(hunterNumber)) {
            LOG.warn("Invalid hunterNumber: {}", hunterNumber);

            throw new InvalidHunterNumberException("Invalid hunterNumber " + hunterNumber);
        }

        final String firstName = person.getFirstName();

        if (firstName.length() < 2) {
            throw new InvalidPersonName(format("Invalid firstName=%s for hunterNumber=%s", firstName, hunterNumber));
        }

        // Length of last name may be one so it is not checked.

        final LocalDate dateOfBirth = person.getDateOfBirth();
        final Period age = DateUtil.calculateAge(dateOfBirth, now());

        if (age.getYears() < InnofactorConstants.MIN_FOREIGN_PERSON_AGE) {
            LOG.warn("Disqualifying date of birth, too young a person: {}", dateOfBirth);
            throw new IllegalAgeException("Too young person, date of birth " + dateOfBirth);
        }

        if (age.getYears() > InnofactorConstants.MAX_PERSON_AGE) {
            LOG.warn("Disqualifying date of birth, too old a person: {}", dateOfBirth);
            throw new IllegalAgeException("Too old person, date of birth " + dateOfBirth);
        }

        return person;
    }

    private static boolean shouldFilterOut(final MetsastajaRekisteriPerson person) {
        // The following conditions are agreed with Innofactor.

        if (StringUtils.isBlank(person.getHunterNumber())) {
            return true;
        }

        final String ssn = person.getSsn();

        // Persons with valid Finnish social security number are not foreign persons.
        if (StringUtils.isNotBlank(ssn) && !FOREIGN_PERSON_SSN_PATTERN.matcher(ssn).matches()) {
            return true;
        }

        if (StringUtils.isBlank(person.getFirstName()) || StringUtils.isBlank(person.getLastName())) {
            // Skip person with missing name.
            return true;
        }

        if (person.getDateOfBirth() == null) {
            return true;
        }

        final String rhyCode = person.getMembershipRhyOfficialCode();

        // Foreign person must have rhy code
        // If person has no valid hunter exams, rhy code must be
        // Ahvenanmaa or foreign person rhy code ('visiting' foreigner).
        if (rhyCode == null) {
            return true;

        } else if (person.getHunterExamDate() == null &&
                !rhyCode.equals(InnofactorConstants.RHY_AHVENANMAA) &&
                !rhyCode.equals(InnofactorConstants.RHY_FOREIGN_MEMBER_CODE)) {
            return true;
        }

        return false;
    }
}
