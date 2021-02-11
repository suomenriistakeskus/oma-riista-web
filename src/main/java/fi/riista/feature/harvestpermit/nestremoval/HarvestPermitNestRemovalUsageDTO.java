package fi.riista.feature.harvestpermit.nestremoval;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HarvestPermitNestRemovalUsageDTO {

    private int speciesCode;

    private Integer usedNestAmount;
    private Integer permitNestAmount;

    private Integer usedEggAmount;
    private Integer permitEggAmount;

    private Integer usedConstructionAmount;
    private Integer permitConstructionAmount;

    @DoNotValidate // Used only as output
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LastModifierDTO lastModifier;

    @Valid
    private List<HarvestPermitNestLocationDTO> nestLocations = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean canEdit;

    public static List<HarvestPermitNestRemovalUsageDTO> createUsage(final Map<Integer, HarvestPermitSpeciesAmount> speciesCodeToAmount,
                                                                     final List<HarvestPermitNestRemovalUsage> usages,
                                                                     final Map<HarvestPermitNestRemovalUsage, LastModifierDTO> lastModifierMapping,
                                                                     final boolean canEdit) {
        final Map<HarvestPermitSpeciesAmount, HarvestPermitNestRemovalUsage> speciesToUsage =
                F.index(usages, usage -> usage.getHarvestPermitSpeciesAmount());

        return speciesCodeToAmount.keySet().stream()
                .map(speciesCode -> {
                    final HarvestPermitSpeciesAmount speciesAmount = speciesCodeToAmount.get(speciesCode);
                    final HarvestPermitNestRemovalUsage usage = speciesToUsage.get(speciesCodeToAmount.get(speciesCode));
                    final LastModifierDTO lastModifierDTO = lastModifierMapping.get(usage);
                    return new HarvestPermitNestRemovalUsageDTO(speciesCode, speciesAmount, usage, lastModifierDTO, canEdit);
                })
                .collect(Collectors.toList());
    }

    public HarvestPermitNestRemovalUsageDTO() {}

    public HarvestPermitNestRemovalUsageDTO(final int speciesCode,
                                            final HarvestPermitSpeciesAmount speciesAmount,
                                            final HarvestPermitNestRemovalUsage usage,
                                            final LastModifierDTO lastModifierDTO,
                                            final boolean canEdit) {

        this.speciesCode = speciesCode;

        this.permitNestAmount = speciesAmount.getNestAmount();
        this.permitEggAmount = speciesAmount.getEggAmount();
        this.permitConstructionAmount = speciesAmount.getConstructionAmount();

        if (usage != null) {
            this.usedNestAmount = usage.getNestAmount();
            this.usedEggAmount = usage.getEggAmount();
            this.usedConstructionAmount = usage.getConstructionAmount();

            usage.getHarvestPermitNestLocations().forEach(nestLocation ->
                    this.nestLocations.add(
                            new HarvestPermitNestLocationDTO(nestLocation.getGeoLocation(), nestLocation.getHarvestPermitNestLocationType())));

            this.lastModifier = lastModifierDTO;
        }

        this.canEdit = canEdit;
    }

    @AssertTrue
    public boolean isValidUsageAmount() {
        final boolean isReportedButNotInPermit = (permitNestAmount == null && usedNestAmount != null) ||
                (permitEggAmount == null && usedEggAmount != null) ||
                (permitConstructionAmount == null && usedConstructionAmount != null);
        final boolean anyReported = usedNestAmount != null || usedEggAmount != null || usedConstructionAmount != null;
        return !isReportedButNotInPermit && anyReported;
    }

    // Accessors -->


    public int getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(final int speciesCode) {
        this.speciesCode = speciesCode;
    }

    public Integer getUsedNestAmount() {
        return usedNestAmount;
    }

    public void setUsedNestAmount(final Integer usedNestAmount) {
        this.usedNestAmount = usedNestAmount;
    }

    public Integer getPermitNestAmount() {
        return permitNestAmount;
    }

    public void setPermitNestAmount(final Integer permitNestAmount) {
        this.permitNestAmount = permitNestAmount;
    }

    public Integer getUsedEggAmount() {
        return usedEggAmount;
    }

    public void setUsedEggAmount(final Integer usedEggAmount) {
        this.usedEggAmount = usedEggAmount;
    }

    public Integer getPermitEggAmount() {
        return permitEggAmount;
    }

    public void setPermitEggAmount(final Integer permitEggAmount) {
        this.permitEggAmount = permitEggAmount;
    }

    public Integer getUsedConstructionAmount() {
        return usedConstructionAmount;
    }

    public void setUsedConstructionAmount(final Integer usedConstructionAmount) {
        this.usedConstructionAmount = usedConstructionAmount;
    }

    public Integer getPermitConstructionAmount() {
        return permitConstructionAmount;
    }

    public void setPermitConstructionAmount(final Integer permitConstructionAmount) {
        this.permitConstructionAmount = permitConstructionAmount;
    }

    public List<HarvestPermitNestLocationDTO> getNestLocations() {
        return nestLocations;
    }

    public void setNestLocations(final List<HarvestPermitNestLocationDTO> nestLocations) {
        this.nestLocations = nestLocations;
    }

    public LastModifierDTO getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(final LastModifierDTO lastModifier) {
        this.lastModifier = lastModifier;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(final boolean canEdit) {
        this.canEdit = canEdit;
    }
}
