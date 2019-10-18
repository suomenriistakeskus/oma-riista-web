package fi.riista.integration.metsastajarekisteri;

import fi.riista.integration.metsastajarekisteri.person.DeletionCode;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class InnofactorImportFileFieldSetMapper implements FieldSetMapper<InnofactorImportFileLine> {
    private static final Logger LOG = LoggerFactory.getLogger(InnofactorImportFileFieldSetMapper.class);

    private static final int EXPECTED_FIELD_COUNT = 35;
    private static final int EXPECTED_FIELD_COUNT_WITHOUT_DATE_OF_BIRTH = EXPECTED_FIELD_COUNT - 1;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    private static final String BOOLEAN_TRUE = "1";

    private static String formatRow(final FieldSet fieldSet) {
        final List<String> strings = new ArrayList<>();
        for (int i = 0; i < fieldSet.getFieldCount(); ++i) {
            strings.add(fieldSet.readString(i));
        }
        return StringUtils.join(strings, "\t");
    }

    @Override
    public InnofactorImportFileLine mapFieldSet(final FieldSet fieldSet) {
        final int fieldCount = fieldSet.getFieldCount();

        if (fieldCount <= 1) {
            LOG.debug("Ignoring invalid row: {}", formatRow(fieldSet));
            return null;
        }

        if (fieldCount != EXPECTED_FIELD_COUNT && fieldCount != EXPECTED_FIELD_COUNT_WITHOUT_DATE_OF_BIRTH) {
            LOG.error("Invalid field set with field count {}, expexcted {}", fieldCount, EXPECTED_FIELD_COUNT);
            LOG.debug("Invalid row was: {}", formatRow(fieldSet));

            throw new IllegalStateException(MessageFormat.format(
                    "Invalid field set with field count {0}, expected {1}", fieldCount, EXPECTED_FIELD_COUNT));
        }

        final InnofactorImportFileLine person = new InnofactorImportFileLine();

        int fieldCounter = 0;

        person.setHunterNumber(fieldSet.readString(fieldCounter++));
        person.setSsn(fieldSet.readString(fieldCounter++));
        person.setModificationDate(readLocalDate(fieldSet, fieldCounter++));
        person.setDeletionDate(readLocalDate(fieldSet, fieldCounter++));
        person.setDeletionStatus(parseDeletionCode(fieldSet, fieldCounter++));
        person.setLastName(fieldSet.readString(fieldCounter++));
        person.setFirstName(fieldSet.readString(fieldCounter++));
        person.setLanguageCode(fieldSet.readString(fieldCounter++));
        person.setMagazineLanguageCode(fieldSet.readString(fieldCounter++));
        person.setEmail(fieldSet.readString(fieldCounter++));
        person.setHomeMunicipalityCode(fieldSet.readString(fieldCounter++));
        person.setMembershipRhyOfficialCode(fieldSet.readString(fieldCounter++));
        person.setHuntingPaymentOneDay(readLocalDate(fieldSet, fieldCounter++));
        person.setHuntingPaymentTwoDay(readLocalDate(fieldSet, fieldCounter++));
        person.setHuntingPaymentOneYear(readInteger(fieldSet, fieldCounter++));
        person.setHuntingPaymentTwoYear(readInteger(fieldSet, fieldCounter++));
        person.setHuntingCardStart(readLocalDate(fieldSet, fieldCounter++));
        person.setHuntingCardEnd(readLocalDate(fieldSet, fieldCounter++));
        person.setHunterExamDate(readLocalDate(fieldSet, fieldCounter++));
        person.setHunterExamExpirationDate(readLocalDate(fieldSet, fieldCounter++));
        person.setHuntingBanStart(readLocalDate(fieldSet, fieldCounter++));
        person.setHuntingBanEnd(readLocalDate(fieldSet, fieldCounter++));
        person.setStreetAddress(fieldSet.readString(fieldCounter++));
        person.setPostalCode(fieldSet.readString(fieldCounter++));
        person.setPostOffice(fieldSet.readString(fieldCounter++));
        person.setCountryCode(fieldSet.readString(fieldCounter++));
        person.setCountryName(fieldSet.readString(fieldCounter++));
        person.setForbidMagazine(fieldSet.readBoolean(fieldCounter++, BOOLEAN_TRUE));
        person.setForbidAddressDelegation(fieldSet.readBoolean(fieldCounter++, BOOLEAN_TRUE));
        person.setForbidPosting(fieldSet.readBoolean(fieldCounter++, BOOLEAN_TRUE));
        person.setInvoiceReferenceCurrent(fieldSet.readString(fieldCounter++));
        person.setInvoiceReferencePrevious(fieldSet.readString(fieldCounter++));
        person.setInvoiceReferenceCurrentYear(readInteger(fieldSet, fieldCounter++));
        person.setInvoiceReferencePreviousYear(readInteger(fieldSet, fieldCounter++));

        if (fieldCount == EXPECTED_FIELD_COUNT) {
            person.setDateOfBirth(readLocalDate(fieldSet, fieldCounter++));
        }

        return person;
    }

    private static DeletionCode parseDeletionCode(final FieldSet fieldSet, final int fieldCounter) {
        return DeletionCode.parse(fieldSet.readString(fieldCounter));
    }

    private static Integer readInteger(final FieldSet fieldSet, final int fieldCounter) {
        final String rawValue = fieldSet.readString(fieldCounter);

        if (StringUtils.isNotBlank(rawValue)) {
            return Integer.parseInt(rawValue);
        }

        return null;
    }

    private static LocalDate readLocalDate(final FieldSet fieldSet, final int fieldCounter) {
        final String rawValue = fieldSet.readString(fieldCounter);

        if (StringUtils.isNotBlank(rawValue)) {
            return DATE_TIME_FORMATTER.parseLocalDate(rawValue);
        }

        return null;
    }
}
