package fi.riista.feature.account;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.Objects;

public class MyOccupationDTO extends BaseEntityDTO<Long> implements HasBeginAndEndDate {

    public static MyOccupationDTO create(@Nonnull final Occupation occupation) {
        Objects.requireNonNull(occupation);

        final MyOccupationDTO dto = new MyOccupationDTO();
        DtoUtil.copyBaseFields(occupation, dto);

        dto.setOccupationType(occupation.getOccupationType());
        dto.setBeginDate(occupation.getBeginDate());
        dto.setEndDate(occupation.getEndDate());
        dto.setCallOrder(occupation.getCallOrder());
        dto.setAdditionalInfo(occupation.getAdditionalInfo());

        dto.setOrganisation(new OrganisationDTO(occupation.getOrganisation()));

        dto.setNameVisibility(occupation.isNameVisibility());
        dto.setPhoneNumberVisibility(occupation.isPhoneNumberVisibility());
        dto.setEmailVisibility(occupation.isEmailVisibility());

        return dto;
    }

    private Long id;
    private Integer rev;

    private OccupationType occupationType;
    private LocalDate beginDate;
    private LocalDate endDate;
    private Integer callOrder;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    @Valid
    private OrganisationDTO organisation;

    private boolean nameVisibility;
    private boolean phoneNumberVisibility;
    private boolean emailVisibility;

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

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCallOrder() {
        return callOrder;
    }

    public void setCallOrder(Integer callOrder) {
        this.callOrder = callOrder;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public OrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationDTO organisation) {
        this.organisation = organisation;
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
