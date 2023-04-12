package fi.riista.feature.huntingclub.members.rhy;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubSubtype;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;

public class RhyClubOccupationDTO implements HasID<Long> {

    public static RhyClubOccupationDTO createForClub(final Occupation occupation, final HuntingClub club) {
        final RhyClubOccupationDTO dto = new RhyClubOccupationDTO();
        dto.setId(occupation.getId());
        dto.setClub(OrganisationNameDTO.create(club));
        dto.setClubSubtype(club.getSubtype());
        dto.setCallOrder(occupation.getCallOrder());
        writePersonData(dto, occupation.getPerson());
        return dto;
    }

    public static RhyClubOccupationDTO createForGroup(final Occupation occupation,
                                                      final HuntingClubGroup group,
                                                      final HuntingClub club) {
        final RhyClubOccupationDTO dto = new RhyClubOccupationDTO();
        dto.setId(occupation.getId());
        dto.setClub(OrganisationNameDTO.create(club));
        dto.setClubSubtype(club.getSubtype());
        dto.setGroup(OrganisationNameDTO.create(group));
        dto.setCallOrder(occupation.getCallOrder());
        writePersonData(dto, occupation.getPerson());
        return dto;
    }

    private static void writePersonData(RhyClubOccupationDTO dto, Person person) {
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setEmail(person.getEmail());
    }

    private long id;
    private OrganisationNameDTO club;
    private HuntingClubSubtype clubSubtype;
    private OrganisationNameDTO group;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Integer callOrder;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long occupationId) {
        this.id = occupationId;
    }

    public OrganisationNameDTO getClub() {
        return club;
    }

    public void setClub(OrganisationNameDTO club) {
        this.club = club;
    }

    public HuntingClubSubtype getClubSubtype() {
        return clubSubtype;
    }

    public void setClubSubtype(final HuntingClubSubtype clubSubtype) {
        this.clubSubtype = clubSubtype;
    }

    public OrganisationNameDTO getGroup() {
        return group;
    }

    public void setGroup(OrganisationNameDTO group) {
        this.group = group;
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

    public Integer getCallOrder() {
        return callOrder;
    }

    public void setCallOrder(final Integer callOrder) {
        this.callOrder = callOrder;
    }
}
