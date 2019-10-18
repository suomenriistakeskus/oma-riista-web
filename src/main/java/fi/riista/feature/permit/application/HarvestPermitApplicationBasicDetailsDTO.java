package fi.riista.feature.permit.application;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDateTime;

import java.util.Optional;

public class HarvestPermitApplicationBasicDetailsDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;

    private HarvestPermitApplication.Status status;
    private PersonContactInfoDTO contactPerson;
    private PermitHolderDTO permitHolder;
    private OrganisationNameDTO huntingClub;
    private Integer applicationNumber;
    private String applicationName;
    private HarvestPermitCategory harvestPermitCategory;
    private int huntingYear;
    private LocalDateTime submitDate;

    public HarvestPermitApplicationBasicDetailsDTO(final HarvestPermitApplication entity) {
        DtoUtil.copyBaseFields(entity, this);

        this.status = entity.getStatus();
        this.applicationNumber = entity.getApplicationNumber();
        this.applicationName = entity.getApplicationName();
        this.harvestPermitCategory = entity.getHarvestPermitCategory();
        this.huntingYear = entity.getApplicationYear();
        this.submitDate = entity.getSubmitDate() != null ? entity.getSubmitDate().toLocalDateTime() : null;
        this.contactPerson = Optional.ofNullable(entity.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null);
        this.permitHolder = entity.getPermitHolder() != null ?
                PermitHolderDTO.createFrom(entity.getPermitHolder())
                : null;
        this.huntingClub = OrganisationNameDTO.createWithOfficialCode(entity.getHuntingClub());
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

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolderDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public OrganisationNameDTO getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(final OrganisationNameDTO huntingClub) {
        this.huntingClub = huntingClub;
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

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
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
