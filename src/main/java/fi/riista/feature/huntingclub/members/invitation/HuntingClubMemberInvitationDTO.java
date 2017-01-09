package fi.riista.feature.huntingclub.members.invitation;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

public class HuntingClubMemberInvitationDTO extends BaseEntityDTO<Long> {

    public static @Nonnull List<HuntingClubMemberInvitationDTO> create(
            @Nonnull List<HuntingClubMemberInvitation> invitations) {
        return create(invitations, false);
    }

    public static @Nonnull List<HuntingClubMemberInvitationDTO> create(
            @Nonnull List<HuntingClubMemberInvitation> invitations, final boolean showForContactPerson) {

        return F.mapNonNullsToList(invitations, inv -> inv == null ? null : create(inv, showForContactPerson));
    }

    public static @Nonnull
    HuntingClubMemberInvitationDTO create(@Nonnull HuntingClubMemberInvitation entity, boolean showForContactPerson) {
        HuntingClubMemberInvitationDTO dto = new HuntingClubMemberInvitationDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setClubId(entity.getHuntingClub().getId());
        dto.setPersonId(entity.getPerson().getId());

        dto.setClub(OrganisationDTO.create(entity.getHuntingClub()));
        dto.setPerson(createPersonDTO(entity.getPerson()));
        if (showForContactPerson) {
            dto.getPerson().setRegistered(entity.getPerson().isRegistered());
        }

        dto.setUserRejectedTime(entity.getUserRejectedTime());
        return dto;
    }

    private static PersonDTO createPersonDTO(Person person) {
        PersonDTO personDTO = new PersonDTO();
        personDTO.setByName(person.getByName());
        personDTO.setLastName(person.getLastName());
        personDTO.setHunterNumber(person.getHunterNumber());
        return personDTO;
    }

    private Long id;
    private Integer rev;

    private Long clubId;
    private Long personId;

    @Valid
    private OrganisationDTO club;

    @Valid
    private PersonDTO person;

    private OccupationType occupationType;

    private DateTime userRejectedTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public OrganisationDTO getClub() {
        return club;
    }

    public void setClub(OrganisationDTO club) {
        this.club = club;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public DateTime getUserRejectedTime() {
        return userRejectedTime;
    }

    public void setUserRejectedTime(DateTime userRejectedTime) {
        this.userRejectedTime = userRejectedTime;
    }
}
