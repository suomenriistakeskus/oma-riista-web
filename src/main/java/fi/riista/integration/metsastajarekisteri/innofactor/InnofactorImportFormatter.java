package fi.riista.integration.metsastajarekisteri.innofactor;

import fi.riista.integration.metsastajarekisteri.MetsastajaRekisteriPerson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.batch.item.ItemProcessor;

public class InnofactorImportFormatter implements ItemProcessor<InnofactorImportFileLine, MetsastajaRekisteriPerson> {

    public static final char[] CAPITALIZATION_DELIMITERS = new char[]{' ', '-', '.', ',', '/', ':', '(', ')'};

    @Override
    public MetsastajaRekisteriPerson process(final InnofactorImportFileLine item) {
        final MetsastajaRekisteriPerson person = new MetsastajaRekisteriPerson();

        person.setDeletionDate(item.getDeletionDate());

        if (item.getDeletionStatus() == InnofactorImportFileLine.DeletedCode.DECEASED) {
            person.setDeletionCode(MetsastajaRekisteriPerson.DeletionCode.DECEASED);

        } else if (item.getDeletionStatus() != null || item.getDeletionDate() != null) {
            person.setDeletionCode(MetsastajaRekisteriPerson.DeletionCode.OTHER);
        }

        person.setSsn(uppercaseAndTrim(item.getSsn()));
        person.setHunterNumber(trim(item.getHunterNumber()));
        person.setMembershipRhyOfficialCode(zeroPad(item.getMembershipRhyOfficialCode(), 3));
        person.setHomeMunicipalityCode(zeroPad(item.getHomeMunicipalityCode(), 3));
        person.setLanguageCode(lowercaseAndTrim(item.getLanguageCode()));
        person.setMagazineLanguageCode(lowercaseAndTrim(item.getMagazineLanguageCode()));
        person.setEmail(item.getEmail());

        person.setFirstName(capitalizeAndTrim(item.getFirstName()));
        person.setLastName(capitalizeAndTrim(item.getLastName()));

        person.setStreetAddress(capitalizeAndTrim(item.getStreetAddress()));
        person.setPostalCode(zeroPad(item.getPostalCode(), 5));
        person.setPostOffice(capitalizeAndTrim(item.getPostOffice()));
        person.setCountryCode(uppercaseAndTrim(item.getCountryCode()));
        person.setCountryName(capitalizeAndTrim(item.getCountryName()));

        person.setHunterExamDate(item.getHunterExamDate());
        person.setHunterExamExpirationDate(item.getHunterExamExpirationDate());
        person.setHuntingCardStart(item.getHuntingCardStart());
        person.setHuntingCardEnd(item.getHuntingCardEnd());
        person.setHuntingPaymentOneDay(item.getHuntingPaymentOneDay());
        person.setHuntingPaymentOneYear(item.getHuntingPaymentOneYear());
        person.setHuntingPaymentTwoDay(item.getHuntingPaymentTwoDay());
        person.setHuntingPaymentTwoYear(item.getHuntingPaymentTwoYear());
        person.setInvoiceReferenceCurrent(StringUtils.trimToNull(item.getInvoiceReferenceCurrent()));
        person.setInvoiceReferencePrevious(StringUtils.trimToNull(item.getInvoiceReferencePrevious()));
        person.setInvoiceReferenceCurrentYear(item.getInvoiceReferenceCurrentYear());
        person.setInvoiceReferencePreviousYear(item.getInvoiceReferencePreviousYear());
        person.setHuntingBanStart(item.getHuntingBanStart());
        person.setHuntingBanEnd(item.getHuntingBanEnd());

        person.setForbidAddressDelegation(item.isForbidAddressDelegation());
        person.setForbidPosting(item.isForbidPosting());
        person.setDenyMagazine(item.isForbidMagazine());

        return person;
    }

    private static String capitalizeAndTrim(final String value) {
        return WordUtils.capitalizeFully(StringUtils.trimToNull(value), CAPITALIZATION_DELIMITERS);
    }

    private static String uppercaseAndTrim(final String value) {
        return StringUtils.upperCase(StringUtils.trimToNull(value));
    }

    private static String lowercaseAndTrim(final String value) {
        return StringUtils.lowerCase(StringUtils.trimToNull(value));
    }

    private static String zeroPad(final String value, int count) {
        return StringUtils.leftPad(StringUtils.trimToNull(value), count, '0');
    }

    private static String trim(final String value) {
        return StringUtils.trimToNull(value);
    }
}
