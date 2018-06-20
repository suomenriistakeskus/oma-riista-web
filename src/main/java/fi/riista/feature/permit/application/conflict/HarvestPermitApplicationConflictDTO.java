package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.Optional;

public class HarvestPermitApplicationConflictDTO extends BaseEntityDTO<Long> {

    public static class AreaDTO extends BaseEntityDTO<Long> {
        private Long id;
        private Integer rev;

        @NotBlank
        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String nameFI;

        @NotBlank
        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String nameSV;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String externalId;

        private AreaDTO(final HarvestPermitArea area) {
            DtoUtil.copyBaseFields(area, this);

            this.rev = area.getConsistencyVersion();
            this.nameFI = area.getNameFinnish();
            this.nameSV = area.getNameSwedish();
            this.externalId = area.getExternalId();
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

        public String getNameFI() {
            return nameFI;
        }

        public void setNameFI(final String nameFI) {
            this.nameFI = nameFI;
        }

        public String getNameSV() {
            return nameSV;
        }

        public void setNameSV(final String nameSV) {
            this.nameSV = nameSV;
        }

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(final String externalId) {
            this.externalId = externalId;
        }
    }

    private Long id;
    private Integer rev;

    private String permitNumber;
    private PersonContactInfoDTO contactPerson;
    private OrganisationNameDTO permitHolder;
    private AreaDTO area;
    private OrganisationNameDTO rhy;
    private boolean onlyPrivateConflicts;
    private boolean onlyMhConflicts;
    private Double conflictSum;

    public static HarvestPermitApplicationConflictDTO create(final HarvestPermitApplication entity,
                                                             final Organisation holder,
                                                             final Organisation rhy,
                                                             final HarvestPermitArea area,
                                                             final boolean onlyPrivateConflicts,
                                                             final boolean onlyMhConflicts,
                                                             final Double conflictSum) {

        final HarvestPermitApplicationConflictDTO dto = new HarvestPermitApplicationConflictDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setPermitNumber(entity.getPermitNumber());
        dto.setContactPerson(Optional.ofNullable(entity.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null));
        dto.setPermitHolder(holder != null ? OrganisationNameDTO.create(holder) : null);
        dto.setRhy(OrganisationNameDTO.create(rhy));
        dto.setArea(new AreaDTO(area));
        dto.setOnlyPrivateConflicts(onlyPrivateConflicts);
        dto.setOnlyMhConflicts(onlyMhConflicts);
        dto.setConflictSum(conflictSum);

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

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
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

    public AreaDTO getArea() {
        return area;
    }

    public void setArea(AreaDTO area) {
        this.area = area;
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
}
