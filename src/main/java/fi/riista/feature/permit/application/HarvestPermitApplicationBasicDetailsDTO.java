package fi.riista.feature.permit.application;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDateTime;

import java.util.Optional;

public class HarvestPermitApplicationBasicDetailsDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;

    private HarvestPermitApplication.Status status;
    private PersonContactInfoDTO contactPerson;
    private HarvestPermitApplicationSummaryDTO.PermitHolderDTO permitHolder;
    private Integer applicationNumber;
    private String applicationName;
    private String permitTypeCode;
    private int huntingYear;
    private LocalDateTime submitDate;

    public HarvestPermitApplicationBasicDetailsDTO(final HarvestPermitApplication entity) {
        DtoUtil.copyBaseFields(entity, this);

        this.status = entity.getStatus();
        this.applicationNumber = entity.getApplicationNumber();
        this.applicationName = entity.getApplicationName();
        this.permitTypeCode = entity.getPermitTypeCode();
        this.huntingYear = entity.getHuntingYear();
        this.submitDate = entity.getSubmitDate() != null ? entity.getSubmitDate().toLocalDateTime() : null;
        this.contactPerson = Optional.ofNullable(entity.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null);
        this.permitHolder = entity.getPermitHolder() != null ?
                new HarvestPermitApplicationSummaryDTO.PermitHolderDTO(entity.getPermitHolder())
                : null;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return this.rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public void setStatus(final HarvestPermitApplication.Status status) {
        this.status = status;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonContactInfoDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public HarvestPermitApplicationSummaryDTO.PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final HarvestPermitApplicationSummaryDTO.PermitHolderDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final LocalDateTime submitDate) {
        this.submitDate = submitDate;
    }
}
