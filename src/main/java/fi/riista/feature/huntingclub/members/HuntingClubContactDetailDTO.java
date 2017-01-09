package fi.riista.feature.huntingclub.members;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;

public class HuntingClubContactDetailDTO {

    public static HuntingClubContactDetailDTO createForGroup(final Occupation occupation) {
        final HuntingClubContactDetailDTO dto = new HuntingClubContactDetailDTO();
        final Organisation group = occupation.getOrganisation();
        dto.setClub(OrganisationNameDTO.create(group.getParentOrganisation()));
        dto.setPrimary(Integer.valueOf(0).equals(occupation.getCallOrder()));
        personData(dto, occupation.getPerson());
        return dto;
    }

    private static void personData(HuntingClubContactDetailDTO dto, Person person) {
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setEmail(person.getEmail());
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

    public void setClub(OrganisationNameDTO club) {
        this.club = club;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(final boolean primary) {
        this.primary = primary;
    }
}
