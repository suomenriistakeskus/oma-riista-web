package fi.riista.feature.huntingclub.members.invitation.mobile;

import fi.riista.feature.account.mobile.MobileOrganisationDTO;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

public class MobileHuntingClubMemberInvitationDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static List<MobileHuntingClubMemberInvitationDTO> create(@Nonnull final List<HuntingClubMemberInvitation> invitations) {
        return F.mapNonNullsToList(invitations, inv -> inv == null ? null : create(inv));
    }

    @Nonnull
    public static MobileHuntingClubMemberInvitationDTO create(@Nonnull final HuntingClubMemberInvitation entity) {
        final MobileHuntingClubMemberInvitationDTO dto = new MobileHuntingClubMemberInvitationDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setClubId(entity.getHuntingClub().getId());
        dto.setPersonId(entity.getPerson().getId());

        dto.setClub(MobileOrganisationDTO.create(entity.getHuntingClub()));
        dto.setPerson(PersonContactInfoDTO.create(entity.getPerson()));
        dto.setUserRejectedTime(entity.getUserRejectedTime());

        return dto;
    }

    private Long id;
    private Integer rev;

    private Long clubId;
    private Long personId;

    @Valid
    private MobileOrganisationDTO club;

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

    public MobileOrganisationDTO getClub() {
        return club;
    }

    public void setClub(final MobileOrganisationDTO club) {
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
