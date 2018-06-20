package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.FinnishHuntingPermitNumber;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class HarvestPermitListDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static HarvestPermitListDTO create(@Nonnull final HarvestPermit permit) {
        Objects.requireNonNull(permit, "permit must not be null");

        final HarvestPermitListDTO dto = new HarvestPermitListDTO();
        DtoUtil.copyBaseFields(permit, dto);

        dto.setPermitNumber(permit.getPermitNumber());
        dto.setPermitType(permit.getPermitType());
        dto.setHarvestReportState(permit.getHarvestReportState());
        dto.setSpeciesAmounts(F.mapNonNullsToList(permit.getSpeciesAmounts(), HarvestPermitSpeciesAmountDTO::create));

        final List<HarvestPermitContactPersonDTO> contactPersons = permit.getContactPersons().stream()
                .map(HarvestPermitContactPerson::getContactPerson)
                .map(HarvestPermitContactPersonDTO::create)
                .collect(toList());
        contactPersons.add(HarvestPermitContactPersonDTO.create(permit.getOriginalContactPerson()));
        dto.setContactPersons(contactPersons);

        return dto;
    }

    private Long id;
    private Integer rev;

    @FinnishHuntingPermitNumber
    private String permitType;
    private String permitNumber;
    private HarvestReportState harvestReportState;
    private List<HarvestPermitSpeciesAmountDTO> speciesAmounts;
    private List<HarvestPermitContactPersonDTO> contactPersons;

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

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(final String permitType) {
        this.permitType = permitType;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public List<HarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<HarvestPermitSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public List<HarvestPermitContactPersonDTO> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(final List<HarvestPermitContactPersonDTO> contactPersons) {
        this.contactPersons = contactPersons;
    }
}
