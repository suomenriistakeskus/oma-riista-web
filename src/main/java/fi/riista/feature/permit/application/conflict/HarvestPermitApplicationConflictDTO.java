package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.util.DtoUtil;
import fi.riista.util.NumberUtils;

import java.util.Optional;

public class HarvestPermitApplicationConflictDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;
    private int applicationNumber;
    private PersonContactInfoDTO contactPerson;
    private OrganisationNameDTO permitHolder;
    private OrganisationNameDTO rhy;
    private boolean onlyPrivateConflicts;
    private boolean onlyMhConflicts;
    private Double conflictSum;
    private Double conflictWaterSum;
    private Double conflictLandSum;
    private Double conflictPrivateAreaSum;
    private Double conflictPrivateAreaWaterSum;
    private Double conflictPrivateAreaLandSum;

    public static HarvestPermitApplicationConflictDTO create(final HarvestPermitApplication entity,
                                                             final Organisation holder,
                                                             final Organisation rhy,
                                                             final boolean onlyPrivateConflicts,
                                                             final boolean onlyMhConflicts,
                                                             final Double conflictSum,
                                                             final Double conflictWaterSum,
                                                             final Double conflictPrivateAreaSum,
                                                             final Double conflictPrivateAreaWaterSum) {

        final HarvestPermitApplicationConflictDTO dto = new HarvestPermitApplicationConflictDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setApplicationNumber(entity.getApplicationNumber());
        dto.setContactPerson(Optional.ofNullable(entity.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null));
        dto.setPermitHolder(holder != null ? OrganisationNameDTO.create(holder) : null);
        dto.setRhy(OrganisationNameDTO.create(rhy));
        dto.setOnlyPrivateConflicts(onlyPrivateConflicts);
        dto.setOnlyMhConflicts(onlyMhConflicts);
        dto.setConflictSum(conflictSum);
        dto.setConflictWaterSum(conflictWaterSum);
        dto.setConflictLandSum(NumberUtils.nullableDoubleSubtraction(conflictSum, conflictWaterSum));
        dto.setConflictPrivateAreaSum(conflictPrivateAreaSum);
        dto.setConflictPrivateAreaWaterSum(conflictPrivateAreaWaterSum);
        dto.setConflictPrivateAreaLandSum(NumberUtils.nullableDoubleSubtraction(conflictPrivateAreaSum, conflictPrivateAreaWaterSum));

        return dto;
    }

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

    public int getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(int applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonContactInfoDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public OrganisationNameDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(OrganisationNameDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

    public boolean isOnlyPrivateConflicts() {
        return onlyPrivateConflicts;
    }

    public void setOnlyPrivateConflicts(final boolean onlyPrivateConflicts) {
        this.onlyPrivateConflicts = onlyPrivateConflicts;
    }

    public boolean isOnlyMhConflicts() {
        return onlyMhConflicts;
    }

    public void setOnlyMhConflicts(final boolean onlyMhConflicts) {
        this.onlyMhConflicts = onlyMhConflicts;
    }

    public Double getConflictSum() {
        return conflictSum;
    }

    public void setConflictSum(final Double conflictSum) {
        this.conflictSum = conflictSum;
    }

    public Double getConflictWaterSum() {
        return conflictWaterSum;
    }

    public void setConflictWaterSum(final Double conflictWaterSum) {
        this.conflictWaterSum = conflictWaterSum;
    }

    public Double getConflictLandSum() {
        return conflictLandSum;
    }

    public void setConflictLandSum(final Double conflictLandSum) {
        this.conflictLandSum = conflictLandSum;
    }

    public Double getConflictPrivateAreaSum() {
        return conflictPrivateAreaSum;
    }

    public void setConflictPrivateAreaSum(final Double conflictPrivateAreaSum) {
        this.conflictPrivateAreaSum = conflictPrivateAreaSum;
    }

    public Double getConflictPrivateAreaWaterSum() {
        return conflictPrivateAreaWaterSum;
    }

    public void setConflictPrivateAreaWaterSum(final Double conflictPrivateAreaWaterSum) {
        this.conflictPrivateAreaWaterSum = conflictPrivateAreaWaterSum;
    }

    public Double getConflictPrivateAreaLandSum() {
        return conflictPrivateAreaLandSum;
    }

    public void setConflictPrivateAreaLandSum(final Double conflictPrivateAreaLandSum) {
        this.conflictPrivateAreaLandSum = conflictPrivateAreaLandSum;
    }
}
