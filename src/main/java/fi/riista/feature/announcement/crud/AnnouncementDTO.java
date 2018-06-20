package fi.riista.feature.announcement.crud;

import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
            if (this == o) return true;
            if (!(o instanceof OrganisationDTO)) return false;
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

    private Set<OccupationType> occupationTypes;

    @Valid
    private Set<OrganisationDTO> subscriberOrganisations;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String subject;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String body;

    private boolean visibleToAll;

    private boolean sendEmail;

    @AssertTrue
    protected boolean isRecipientsOk() {
        return visibleToAll || !F.isNullOrEmpty(this.occupationTypes);
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
}
