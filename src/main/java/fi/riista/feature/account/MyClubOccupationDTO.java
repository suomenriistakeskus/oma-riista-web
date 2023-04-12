package fi.riista.feature.account;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MyClubOccupationDTO extends BaseEntityDTO<Long> {

    public static MyClubOccupationDTO create(
            @Nonnull final Occupation occupation, final List<Occupation> groupOccupations) {

        final MyClubOccupationDTO dto = create(occupation);

        if (groupOccupations != null) {
            dto.setGroupOccupations(F.mapNonNullsToList(groupOccupations, MyClubOccupationDTO::create));
        } else {
            dto.setGroupOccupations(Collections.emptyList());
        }

        return dto;
    }

    public static MyClubOccupationDTO create(@Nonnull final Occupation occupation) {
        Objects.requireNonNull(occupation);

        final MyClubOccupationDTO dto = new MyClubOccupationDTO();

        DtoUtil.copyBaseFields(occupation, dto);

        dto.setOccupationType(occupation.getOccupationType());
        dto.setCallOrder(occupation.getCallOrder());
        dto.setContactInfoShare(occupation.getContactInfoShare());

        final Organisation org = occupation.getOrganisation();
        dto.setOrganisation(new OrganisationDTO(org));

        if (occupation.getOccupationType().isApplicableFor(OrganisationType.CLUBGROUP)) {
            dto.setParentOrganisation(new OrganisationDTO(org.getParentOrganisation()));
        }
        dto.setBeginDate(occupation.getBeginDate());
        dto.setEndDate(occupation.getEndDate());

        dto.setNameVisibility(occupation.isNameVisibility());
        dto.setPhoneNumberVisibility(occupation.isPhoneNumberVisibility());
        dto.setEmailVisibility(occupation.isEmailVisibility());

        return dto;
    }

    private Long id;
    private Integer rev;

    private OccupationType occupationType;
    private Integer callOrder;

    @Valid
    private OrganisationDTO organisation;

    @Valid
    private OrganisationDTO parentOrganisation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private ContactInfoShare contactInfoShare;

    private LocalDate beginDate;
    private LocalDate endDate;

    private List<MyClubOccupationDTO> groupOccupations;

    private boolean nameVisibility;

    private boolean phoneNumberVisibility;

    private boolean emailVisibility;

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

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public Integer getCallOrder() {
        return callOrder;
    }

    public void setCallOrder(final Integer callOrder) {
        this.callOrder = callOrder;
    }

    public OrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(final OrganisationDTO organisation) {
        this.organisation = organisation;
    }

    public OrganisationDTO getParentOrganisation() {
        return parentOrganisation;
    }

    public void setParentOrganisation(final OrganisationDTO parentOrganisation) {
        this.parentOrganisation = parentOrganisation;
    }

    public ContactInfoShare getContactInfoShare() {
        return contactInfoShare;
    }

    public void setContactInfoShare(final ContactInfoShare contactInfoShare) {
        this.contactInfoShare = contactInfoShare;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<MyClubOccupationDTO> getGroupOccupations() {
        return groupOccupations;
    }

    public void setGroupOccupations(final List<MyClubOccupationDTO> groupOccupations) {
        this.groupOccupations = groupOccupations;
    }

    public boolean isNameVisibility() {
        return nameVisibility;
    }

    public void setNameVisibility(final boolean nameVisibility) {
        this.nameVisibility = nameVisibility;
    }

    public boolean isPhoneNumberVisibility() {
        return phoneNumberVisibility;
    }

    public void setPhoneNumberVisibility(final boolean phoneNumberVisibility) {
        this.phoneNumberVisibility = phoneNumberVisibility;
    }

    public boolean isEmailVisibility() {
        return emailVisibility;
    }

    public void setEmailVisibility(final boolean emailVisibility) {
        this.emailVisibility = emailVisibility;
    }
}
