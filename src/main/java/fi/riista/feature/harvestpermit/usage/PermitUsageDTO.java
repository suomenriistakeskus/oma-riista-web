package fi.riista.feature.harvestpermit.usage;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import fi.riista.validation.DoNotValidate;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fi.riista.util.F.mapNullable;
import static java.util.Optional.ofNullable;

public class PermitUsageDTO {

    public static PermitUsageDTO create(final HarvestPermitSpeciesAmount speciesAmount,
                                        final PermitUsage usage,
                                        final List<PermitUsageLocationDTO> usageLocations,
                                        final LastModifierDTO lastModifier) {
        final Long id = ofNullable(usage).map(PermitUsage::getId).orElse(null);
        final int speciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        final Integer permitAmount = mapNullable(speciesAmount.getSpecimenAmount(), Float::intValue);
        final int usedAmount = ofNullable(usage).map(PermitUsage::getSpecimenAmount).orElse(0);
        final Integer permitEggAmount = speciesAmount.getEggAmount();
        final int usedEggAmount = ofNullable(usage).map(PermitUsage::getEggAmount).orElse(0);

        return new PermitUsageDTO(
                id, speciesCode, permitAmount, usedAmount, permitEggAmount, usedEggAmount, usageLocations, lastModifier);
    }

    private Long id;

    private int speciesCode;

    private Integer permitSpecimenAmount;

    @Min(0)
    private int usedSpecimenAmount;

    private Integer permitEggAmount;

    @Min(0)
    private int usedEggAmount;

    @Valid
    private List<PermitUsageLocationDTO> permitUsageLocations = new ArrayList<>();

    @DoNotValidate // Used only as output
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LastModifierDTO lastModifier;

    public PermitUsageDTO() {
    }

    public PermitUsageDTO(final Long id,
                          final int speciesCode,
                          final Integer permitSpecimenAmount,
                          final int usedSpecimenAmount,
                          final Integer permitEggAmount,
                          final int usedEggAmount,
                          final @Valid List<PermitUsageLocationDTO> permitUsageLocations,
                          final @Valid LastModifierDTO lastModifier) {
        this.id = id;
        this.speciesCode = speciesCode;
        this.permitSpecimenAmount = permitSpecimenAmount;
        this.usedSpecimenAmount = usedSpecimenAmount;
        this.permitEggAmount = permitEggAmount;
        this.usedEggAmount = usedEggAmount;
        this.permitUsageLocations = permitUsageLocations;
        this.lastModifier = lastModifier;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public int getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(final int speciesCode) {
        this.speciesCode = speciesCode;
    }

    public int getPermitSpecimenAmount() {
        return permitSpecimenAmount;
    }

    public void setPermitSpecimenAmount(final int permitSpecimenAmount) {
        this.permitSpecimenAmount = permitSpecimenAmount;
    }

    public int getUsedSpecimenAmount() {
        return usedSpecimenAmount;
    }

    public void setUsedSpecimenAmount(final int usedSpecimenAmount) {
        this.usedSpecimenAmount = usedSpecimenAmount;
    }

    public Integer getPermitEggAmount() {
        return permitEggAmount;
    }

    public void setPermitEggAmount(final Integer permitEggAmount) {
        this.permitEggAmount = permitEggAmount;
    }

    public int getUsedEggAmount() {
        return usedEggAmount;
    }

    public void setUsedEggAmount(final Integer usedEggAmount) {
        this.usedEggAmount = usedEggAmount;
    }

    public List<PermitUsageLocationDTO> getPermitUsageLocations() {
        return permitUsageLocations;
    }

    public void setPermitUsageLocations(final List<PermitUsageLocationDTO> permitUsageLocations) {
        this.permitUsageLocations = permitUsageLocations;
    }

    public LastModifierDTO getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(final LastModifierDTO lastModifier) {
        this.lastModifier = lastModifier;
    }
}
