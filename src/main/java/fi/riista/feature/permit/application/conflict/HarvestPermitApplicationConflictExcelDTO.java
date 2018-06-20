package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;

public class HarvestPermitApplicationConflictExcelDTO {

    @Nonnull
    public static HarvestPermitApplicationConflictExcelDTO.ContactPerson contactPerson(final Person firstContact) {
        return new HarvestPermitApplicationConflictExcelDTO.ContactPerson(
                firstContact.getFullName(), firstContact.getPhoneNumber(), firstContact.getEmail());
    }

    public static class ContactPerson {
        private final String fullName;
        private final String phoneNumber;
        private final String email;

        public ContactPerson(final String fullName, final String phoneNumber, final String email) {
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getEmail() {
            return email;
        }
    }

    private final Integer firstApplicationNumber;
    private final Integer secondApplicationNumber;

    private final ContactPerson firstApplicationContactPerson;
    private final ContactPerson secondApplicationContactPerson;

    private final String propertyNumber;
    private final String propertyName;
    private final double conflictAreaSize;
    private final boolean metsahallitus;

    public HarvestPermitApplicationConflictExcelDTO(final Integer firstApplicationNumber,
                                                    final Integer secondApplicationNumber,
                                                    final ContactPerson firstApplicationContactPerson,
                                                    final ContactPerson secondApplicationContactPerson,
                                                    final String propertyNumber,
                                                    final String propertyName,
                                                    final double conflictAreaSize,
                                                    final boolean metsahallitus) {
        this.firstApplicationNumber = firstApplicationNumber;
        this.secondApplicationNumber = secondApplicationNumber;
        this.firstApplicationContactPerson = firstApplicationContactPerson;
        this.secondApplicationContactPerson = secondApplicationContactPerson;
        this.propertyNumber = propertyNumber;
        this.propertyName = propertyName;
        this.conflictAreaSize = conflictAreaSize;
        this.metsahallitus = metsahallitus;
    }

    public Integer getFirstApplicationNumber() {
        return firstApplicationNumber;
    }

    public Integer getSecondApplicationNumber() {
        return secondApplicationNumber;
    }

    public ContactPerson getFirstApplicationContactPerson() {
        return firstApplicationContactPerson;
    }

    public ContactPerson getSecondApplicationContactPerson() {
        return secondApplicationContactPerson;
    }

    public String getPropertyNumber() {
        return propertyNumber;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public double getConflictAreaSize() {
        return conflictAreaSize;
    }

    public boolean isMetsahallitus() {
        return metsahallitus;
    }
}
