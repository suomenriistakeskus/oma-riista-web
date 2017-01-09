package fi.riista.integration.metsastajarekisteri.innofactor;

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

    private static final int EXPECTED_FIELD_COUNT = 34;
    private static final int EXPECTED_FIELD_COUNT_WITHOUT_INVOICE_REF = EXPECTED_FIELD_COUNT - 4;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    private static final String BOOLEAN_TRUE = "1";

    private static String formatRow(final FieldSet fieldSet) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < fieldSet.getFieldCount(); ++i) {
            strings.add(fieldSet.readString(i));
        }
        return StringUtils.join(strings, "\t");
    }

    @Override
    public InnofactorImportFileLine mapFieldSet(FieldSet fieldSet) {
        if (fieldSet.getFieldCount() <= 1) {
            LOG.debug("Ignoring invalid row: {}", formatRow(fieldSet));
            return null;
        }

        if (fieldSet.getFieldCount() != EXPECTED_FIELD_COUNT &&
                fieldSet.getFieldCount() != EXPECTED_FIELD_COUNT_WITHOUT_INVOICE_REF) {
            LOG.error("Invalid field set with field count {}, expexcted {}",
                    fieldSet.getFieldCount(), EXPECTED_FIELD_COUNT);
            LOG.debug("Invalid row was: {}", formatRow(fieldSet));
            throw new IllegalStateException(
                    MessageFormat.format("Invalid field set with field count {0}, expected {1}",
                            fieldSet.getFieldCount(), EXPECTED_FIELD_COUNT));
        }

        final InnofactorImportFileLine person = new InnofactorImportFileLine();

        int fieldCounter = 0;

        person.setHunterNumber(fieldSet.readString(fieldCounter++));
        person.setSsn(fieldSet.readString(fieldCounter++));
        person.setModificationDate(readLocalDate(fieldSet, fieldCounter++));
        person.setDeletionDate(readLocalDate(fieldSet, fieldCounter++));
        person.setDeletionStatus(parseDeletedCode(fieldSet, fieldCounter++));
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

        if (fieldSet.getFieldCount() == EXPECTED_FIELD_COUNT) {
            person.setInvoiceReferenceCurrent(fieldSet.readString(fieldCounter++));
            person.setInvoiceReferencePrevious(fieldSet.readString(fieldCounter++));
            person.setInvoiceReferenceCurrentYear(readInteger(fieldSet, fieldCounter++));
            person.setInvoiceReferencePreviousYear(readInteger(fieldSet, fieldCounter++));
        }

        return person;
    }

    private static InnofactorImportFileLine.DeletedCode parseDeletedCode(FieldSet fieldSet, int fieldCounter) {
        return InnofactorImportFileLine.DeletedCode.parse(fieldSet.readString(fieldCounter));
    }

    private static Integer readInteger(FieldSet fieldSet, int fieldCounter) {
        String rawValue = fieldSet.readString(fieldCounter);

        if (StringUtils.isNotBlank(rawValue)) {
            return Integer.parseInt(rawValue);
        }

        return null;
    }

    private static LocalDate readLocalDate(FieldSet fieldSet, int fieldCounter) {
        String rawValue = fieldSet.readString(fieldCounter);

        if (StringUtils.isNotBlank(rawValue)) {
            return DATE_TIME_FORMATTER.parseLocalDate(rawValue);
        }

        return null;
    }

}
