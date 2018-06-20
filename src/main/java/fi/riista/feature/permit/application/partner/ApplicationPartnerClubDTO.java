package fi.riista.feature.permit.application.partner;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;

import java.util.List;

public class ApplicationPartnerClubDTO extends OrganisationNameDTO {
    public static class ContactPerson {
        private final String lastName;
        private final String byName;

        public ContactPerson(final Person person) {
            this.lastName = person.getLastName();
            this.byName = person.getByName();
        }

        public String getLastName() {
            return lastName;
        }

        public String getByName() {
            return byName;
        }
    }

    private List<ContactPerson> contactPersons;

    public ApplicationPartnerClubDTO(final HuntingClub club, final List<Person> contactPersons) {
        setId(club.getId());
        setRev(club.getConsistencyVersion());
        setNameFI(club.getNameFinnish());
        setNameSV(club.getNameSwedish());
        setOfficialCode(club.getOfficialCode());
        setContactPersons(F.mapNonNullsToList(contactPersons, ContactPerson::new));
    }

    public List<ContactPerson> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(final List<ContactPerson> contactPersons) {
        this.contactPersons = contactPersons;
    }
}
