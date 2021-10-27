package fi.riista.feature.harvestpermit.usage;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fi.riista.util.F.mapNullable;

public class PermitUsageDTO {

    public static PermitUsageDTO create(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                        final PermitUsage usage,
                                        final List<PermitUsageLocationDTO> usageLocations,
                                        final LastModifierDTO lastModifier) {
        Objects.requireNonNull(speciesAmount);

        final Long id = F.mapNullable(usage, PermitUsage::getId);
        final int speciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        final Integer permitAmount = mapNullable(speciesAmount.getSpecimenAmount(), Float::intValue);
        final Integer usedAmount = F.mapNullable(usage, PermitUsage::getSpecimenAmount);
        final Integer permitEggAmount = speciesAmount.getEggAmount();
        final Integer usedEggAmount = F.mapNullable(usage, PermitUsage::getEggAmount);

        return new PermitUsageDTO(
                id, speciesCode, permitAmount, usedAmount, permitEggAmount, usedEggAmount, usageLocations, lastModifier);
    }

    private Long id;

    private int speciesCode;

    private Integer permitSpecimenAmount;

    @Min(0)
    private Integer usedSpecimenAmount;

    private Integer permitEggAmount;

    @Min(0)
    private Integer usedEggAmount;

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
                          final Integer usedSpecimenAmount,
                          final Integer permitEggAmount,
                          final Integer usedEggAmount,
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

    public Integer getPermitSpecimenAmount() {
        return permitSpecimenAmount;
    }

    public void setPermitSpecimenAmount(final Integer permitSpecimenAmount) {
        this.permitSpecimenAmount = permitSpecimenAmount;
    }

    public Integer getUsedSpecimenAmount() {
        return usedSpecimenAmount;
    }

    public void setUsedSpecimenAmount(final Integer usedSpecimenAmount) {
        this.usedSpecimenAmount = usedSpecimenAmount;
    }

    public Integer getPermitEggAmount() {
        return permitEggAmount;
    }

    public void setPermitEggAmount(final Integer permitEggAmount) {
        this.permitEggAmount = permitEggAmount;
    }

    public Integer getUsedEggAmount() {
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
