package fi.riista.feature.huntingclub.members;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;

public class HuntingClubContactDetailDTO {

    public static HuntingClubContactDetailDTO createForGroup(final Occupation occupation) {
        final HuntingClubContactDetailDTO dto = new HuntingClubContactDetailDTO();
        final Organisation group = occupation.getOrganisation();
        dto.setClub(OrganisationNameDTO.create(group.getParentOrganisation()));
        dto.setPrimary(Integer.valueOf(0).equals(occupation.getCallOrder()));

        writePersonData(dto, occupation);
        return dto;
    }

    public static HuntingClubContactDetailDTO createOwnLeaderForContactPerson(final Occupation occupation) {
        final HuntingClubContactDetailDTO dto = new HuntingClubContactDetailDTO();
        final Organisation group = occupation.getOrganisation();
        dto.setClub(OrganisationNameDTO.create(group.getParentOrganisation()));
        boolean primary = Integer.valueOf(0).equals(occupation.getCallOrder());
        dto.setPrimary(primary);

        final Person person = occupation.getPerson();
        dto.setLastName(person.getLastName());
        dto.setFirstName(person.getFirstName());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setEmail(person.getEmail());

        return dto;
    }

    private static void writePersonData(final HuntingClubContactDetailDTO dto, final Occupation occupation) {
        final Person person = occupation.getPerson();
        if(occupation.isNameVisibility()) {
            dto.setLastName(person.getLastName());
            dto.setFirstName(person.getFirstName());
        }
        if(occupation.isPhoneNumberVisibility()) {
            dto.setPhoneNumber(person.getPhoneNumber());
        }
        if(occupation.isEmailVisibility()) {
            dto.setEmail(person.getEmail());
        }

    }

    private OrganisationNameDTO club;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private boolean primary;

    public OrganisationNameDTO getClub() {
        return club;
    }

    public void setClub(final OrganisationNameDTO club) {
        this.club = club;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(final boolean primary) {
        this.primary = primary;
    }
}
