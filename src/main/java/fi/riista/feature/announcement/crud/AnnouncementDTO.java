package fi.riista.feature.announcement.crud;

import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static fi.riista.util.F.mapNullable;

public class AnnouncementDTO extends BaseEntityDTO<Long> {

    public static class OrganisationDTO {
        public static OrganisationDTO create(final Organisation organisation) {
            final OrganisationDTO dto = new OrganisationDTO();
            dto.setOrganisationType(organisation.getOrganisationType());
            dto.setOfficialCode(organisation.getOfficialCode());
            return dto;
        }

        public static OrganisationDTO create(final AnnouncementSubscriber subscriber) {
            return create(subscriber.getOrganisation());
        }

        @NotNull
        private OrganisationType organisationType;

        @NotNull
        @Pattern(regexp = "\\d+")
        private String officialCode;

        @AssertTrue
        public boolean isValidOrganisationType() {
            return organisationType == OrganisationType.RK ||
                    organisationType == OrganisationType.RHY ||
                    organisationType == OrganisationType.RKA ||
                    organisationType == OrganisationType.CLUB;
        }

        public OrganisationType getOrganisationType() {
            return organisationType;
        }

        public void setOrganisationType(final OrganisationType organisationType) {
            this.organisationType = organisationType;
        }

        public String getOfficialCode() {
            return officialCode;
        }

        public void setOfficialCode(final String officialCode) {
            this.officialCode = officialCode;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof OrganisationDTO)) {
                return false;
            }
            final OrganisationDTO that = (OrganisationDTO) o;
            return organisationType == that.organisationType &&
                    Objects.equals(officialCode, that.officialCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(organisationType, officialCode);
        }
    }

    public static AnnouncementDTO create(final Announcement announcement,
                                         final List<AnnouncementSubscriber> subscribers) {
        Objects.requireNonNull(announcement, "announcement must not be null");

        final AnnouncementDTO dto = new AnnouncementDTO();

        DtoUtil.copyBaseFields(announcement, dto);

        dto.setBody(announcement.getBody());
        dto.setSubject(announcement.getSubject());
        dto.setVisibleToAll(announcement.isVisibleToAll());

        final Organisation rhyMembershipSubscriber = announcement.getRhyMembershipSubscriber();
        dto.setVisibleToRhyMembers(rhyMembershipSubscriber != null);
        dto.setRhyMembershipSubscriber(mapNullable(rhyMembershipSubscriber, AnnouncementDTO.OrganisationDTO::create));

        if (announcement.getFromOrganisation() != null) {
            dto.setFromOrganisation(AnnouncementDTO.OrganisationDTO.create(announcement.getFromOrganisation()));
        }

        if (subscribers != null) {
            dto.setOccupationTypes(F.mapNonNullsToSet(
                    subscribers, AnnouncementSubscriber::getOccupationType));
            dto.setSubscriberOrganisations(F.mapNonNullsToSet(
                    subscribers, OrganisationDTO::create));
        }

        return dto;
    }

    private Long id;

    private Integer rev;

    @Valid
    @NotNull
    private OrganisationDTO fromOrganisation;

    @Valid
    private OrganisationDTO rhyMembershipSubscriber;

    private Set<OccupationType> occupationTypes;

    // Only required and allowed for moderator
    @Valid
    private Set<OrganisationDTO> subscriberOrganisations;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String subject;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String body;

    private boolean visibleToAll;

    private boolean visibleToRhyMembers;

    private boolean sendEmail;

    @AssertTrue
    public boolean isVisibleToAllAllowed() {
        return !visibleToAll || fromOrganisation != null && fromOrganisation.getOrganisationType() == OrganisationType.RK;
    }

    @AssertTrue
    public boolean isNotEmailSelectedTogetherWithVisibleToAll() {
        return !visibleToAll || !sendEmail;
    }

    @AssertTrue
    public boolean isRhySubscriberValid() {

        // Allowed only for moderators on behalf of RK
        return rhyMembershipSubscriber == null ||
                (visibleToRhyMembers && fromOrganisation.getOrganisationType() == OrganisationType.RK);
    }

    @AssertTrue
    public boolean isVisibleToRhyMembersAllowed() {
        return !visibleToRhyMembers ||
                (fromOrganisation != null &&
                        (fromOrganisation.getOrganisationType() == OrganisationType.RHY ||
                                fromOrganisation.getOrganisationType() == OrganisationType.RK));
    }

    @AssertTrue
    public boolean isRecipientsOk() {
        if (fromOrganisation == null || fromOrganisation.getOrganisationType() == null) {
            return false;
        }

        switch (fromOrganisation.getOrganisationType()) {
            case RK:
                return isValidRecipientsForRiistakeskus();

            case RHY:
                return isValidRecipientsForRhy();

            case CLUB:
                return isValidRecipientsForClub();

            default:
                return false;
        }
    }

    private boolean isValidRecipientsForClub() {
        if (visibleToAll || visibleToRhyMembers) {
            return false;
        }

        if (!isSubscriberEmptyOrMatchesSender()) {
            return false;
        }

        return !F.isNullOrEmpty(occupationTypes) &&
                occupationTypes.stream().allMatch(OccupationType::isClubOrGroupOccupation);
    }

    private boolean isValidRecipientsForRhy() {
        if (visibleToAll || !isSubscriberEmptyOrMatchesSender()) {
            return false;
        }

        if (visibleToRhyMembers) {
            return F.isNullOrEmpty(occupationTypes);
        }

        return !F.isNullOrEmpty(occupationTypes) &&
                occupationTypes.stream().allMatch(t -> t.isRhyOccupation() || t.isClubOrGroupOccupation());
    }

    public boolean isSubscriberEmptyOrMatchesSender() {
        if (F.isNullOrEmpty(subscriberOrganisations)) {
            return true;
        }

        if (subscriberOrganisations.size() != 1) {
            return false;
        }

        final OrganisationDTO subscriber = subscriberOrganisations.iterator().next();
        return subscriber.getOrganisationType() == fromOrganisation.getOrganisationType() &&
                Objects.equals(subscriber.getOfficialCode(), fromOrganisation.getOfficialCode());
    }

    private boolean isValidRecipientsForRiistakeskus() {
        final boolean subscriberOccupationsEmpty = F.isNullOrEmpty(occupationTypes);
        final boolean subscriberOrganisationsEmpty = F.isNullOrEmpty(subscriberOrganisations);

        if (visibleToRhyMembers) {
            return subscriberOccupationsEmpty && subscriberOrganisationsEmpty &&
                    rhyMembershipSubscriber != null;
        }

        if (visibleToAll) {
            return subscriberOccupationsEmpty && subscriberOrganisationsEmpty;
        }

        return !subscriberOccupationsEmpty && !subscriberOrganisationsEmpty;
    }

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

    public OrganisationDTO getFromOrganisation() {
        return fromOrganisation;
    }

    public void setFromOrganisation(final OrganisationDTO fromOrganisation) {
        this.fromOrganisation = fromOrganisation;
    }

    public Set<OccupationType> getOccupationTypes() {
        return occupationTypes;
    }

    public void setOccupationTypes(final Set<OccupationType> occupationTypes) {
        this.occupationTypes = occupationTypes;
    }

    public Set<OrganisationDTO> getSubscriberOrganisations() {
        return subscriberOrganisations;
    }

    public void setSubscriberOrganisations(final Set<OrganisationDTO> subscriberOrganisations) {
        this.subscriberOrganisations = subscriberOrganisations;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(final boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isVisibleToAll() {
        return visibleToAll;
    }

    public void setVisibleToAll(final boolean visibleToAll) {
        this.visibleToAll = visibleToAll;
    }

    public boolean isVisibleToRhyMembers() {
        return visibleToRhyMembers;
    }

    public void setVisibleToRhyMembers(final boolean visibleToRhyMembers) {
        this.visibleToRhyMembers = visibleToRhyMembers;
    }

    public OrganisationDTO getRhyMembershipSubscriber() {
        return rhyMembershipSubscriber;
    }

    public void setRhyMembershipSubscriber(final OrganisationDTO rhyMembershipSubscriber) {
        this.rhyMembershipSubscriber = rhyMembershipSubscriber;
    }
}
