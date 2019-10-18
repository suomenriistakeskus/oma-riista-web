package fi.riista.feature.huntingclub.members.invitation;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

public class HuntingClubMemberInvitationDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static List<HuntingClubMemberInvitationDTO> create(@Nonnull final List<HuntingClubMemberInvitation> invitations) {
        return create(invitations, false);
    }

    @Nonnull
    public static List<HuntingClubMemberInvitationDTO> create(@Nonnull final List<HuntingClubMemberInvitation> invitations,
                                                              final boolean showForContactPerson) {

        return F.mapNonNullsToList(invitations, inv -> inv == null ? null : create(inv, showForContactPerson));
    }

    @Nonnull
    public static HuntingClubMemberInvitationDTO create(@Nonnull final HuntingClubMemberInvitation entity,
                                                        final boolean showForContactPerson) {

        final HuntingClubMemberInvitationDTO dto = new HuntingClubMemberInvitationDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setClubId(entity.getHuntingClub().getId());
        dto.setPersonId(entity.getPerson().getId());

        dto.setClub(OrganisationDTO.create(entity.getHuntingClub()));
        dto.setPerson(createPersonDTO(entity.getPerson(), showForContactPerson));
        dto.setUserRejectedTime(entity.getUserRejectedTime());

        return dto;
    }

    private static PersonContactInfoDTO createPersonDTO(final Person person, final boolean showForContactPerson) {
        final PersonContactInfoDTO dto = new PersonContactInfoDTO();
        dto.setByName(person.getByName());
        dto.setLastName(person.getLastName());
        dto.setHunterNumber(person.getHunterNumber());

        if (showForContactPerson) {
            dto.setRegistered(person.isRegistered());
        }

        return dto;
    }

    private Long id;
    private Integer rev;

    private Long clubId;
    private Long personId;

    @Valid
    private OrganisationDTO club;

    @Valid
    private PersonContactInfoDTO person;

    private OccupationType occupationType;

    private DateTime userRejectedTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(final Long clubId) {
        this.clubId = clubId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(final Long personId) {
        this.personId = personId;
    }

    public OrganisationDTO getClub() {
        return club;
    }

    public void setClub(final OrganisationDTO club) {
        this.club = club;
    }

    public PersonContactInfoDTO getPerson() {
        return person;
    }

    public void setPerson(final PersonContactInfoDTO person) {
        this.person = person;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public DateTime getUserRejectedTime() {
        return userRejectedTime;
    }

    public void setUserRejectedTime(final DateTime userRejectedTime) {
        this.userRejectedTime = userRejectedTime;
    }
}
