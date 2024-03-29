package fi.riista.feature.harvestpermit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class HarvestPermitDTO {


    @Nonnull
    public static HarvestPermitDTO create(final @Nonnull HarvestPermit harvestPermit,
                                          final @Nonnull List<HarvestPermitSpeciesAmount> speciesAmounts,
                                          final @Nonnull Set<String> amendmentPermitNumbers,
                                          final @Nonnull List<HarvestPermitSpeciesAmount> amendmentSpeciesAmounts,
                                          final @Nonnull SystemUser activeUser,
                                          final @Nullable GrantStatus grantStatus,
                                          final @Nullable PermitDecision decision) {
        requireNonNull(harvestPermit);
        requireNonNull(speciesAmounts);
        requireNonNull(amendmentPermitNumbers);
        requireNonNull(amendmentSpeciesAmounts);
        requireNonNull(activeUser);

        final Function<GameSpecies, Float> amendmentAmount = createAmendmentPermitAmountMapping(amendmentSpeciesAmounts);
        final List<HarvestPermitSpeciesAmountWithAmendmentDTO> speciesAmountsDTOs = F.mapNonNullsToList(speciesAmounts,
                speciesAmount -> HarvestPermitSpeciesAmountWithAmendmentDTO.create(
                        speciesAmount, amendmentAmount.apply(speciesAmount.getGameSpecies())));

        final Set<Integer> gameSpeciesCodes = F.mapNonNullsToSet(speciesAmounts,
                spa -> spa.getGameSpecies().getOfficialCode());

        final boolean canAddHarvest = harvestPermit.canAddHarvest(activeUser);
        final boolean canEditHarvest = harvestPermit.canCreateEndOfHuntingReport(activeUser);
        final boolean canDownloadDecision = harvestPermit.getPermitDecision() != null || harvestPermit.getPrintingUrl() != null;

        final PermitDecision.DecisionType decisionType = mapNullable(decision,PermitDecision::getDecisionType);
        final String decisionDocumentNumber = mapNullable(decision, PermitDecision::createPermitNumber);
        return new HarvestPermitDTO(harvestPermit.getId(), harvestPermit.getPermitNumber(),
                harvestPermit.getPermitType(), harvestPermit.getPermitTypeCode(), F.getId(harvestPermit.getOriginalPermit()),
                harvestPermit.getHarvestReportState(),
                gameSpeciesCodes, speciesAmountsDTOs, amendmentPermitNumbers,
                canAddHarvest, canEditHarvest, canDownloadDecision, grantStatus, decisionType, decisionDocumentNumber);
    }

    private static Function<GameSpecies, Float> createAmendmentPermitAmountMapping(
            final List<HarvestPermitSpeciesAmount> amendmentSpeciesAmounts) {
        final Map<Long, Float> amendAmounts = amendmentSpeciesAmounts.stream().collect(toMap(
                spa -> spa.getGameSpecies().getId(),
                HarvestPermitSpeciesAmount::getSpecimenAmount,
                (a, b) -> a + b));

        return species -> amendAmounts.getOrDefault(species.getId(), 0f);
    }

    private HarvestPermitDTO(final @Nonnull Long permitId,
                             final @Nonnull String permitNumber,
                             final @Nonnull String permitType,
                             final @Nonnull String permitTypeCode,
                             final Long originalPermitId,
                             final HarvestReportState harvestReportState,
                             final @Nonnull Set<Integer> gameSpeciesCodes,
                             final @Nonnull List<HarvestPermitSpeciesAmountWithAmendmentDTO> speciesAmounts,
                             final @Nonnull Set<String> amendmentPermitNumbers,
                             final boolean canAddHarvest,
                             final boolean canEditHarvest,
                             final boolean canDownloadDecision,
                             final GrantStatus grantStatus,
                             final PermitDecision.DecisionType decisionType,
                             final String decisionDocumentNumber) {
        this.id = requireNonNull(permitId);
        this.permitNumber = requireNonNull(permitNumber);
        this.permitType = requireNonNull(permitType);
        this.permitTypeCode = requireNonNull(permitTypeCode);
        this.originalPermitId = originalPermitId;
        this.harvestReportState = harvestReportState;
        this.gameSpeciesCodes = requireNonNull(gameSpeciesCodes);
        this.speciesAmounts = requireNonNull(speciesAmounts);
        this.amendmentPermitNumbers = requireNonNull(amendmentPermitNumbers);
        this.canAddHarvest = canAddHarvest;
        this.canEndHunting = canEditHarvest;
        this.canDownloadDecision = canDownloadDecision;
        this.grantStatus = grantStatus;
        this.decisionType = decisionType;
        this.decisionDocumentNumber = decisionDocumentNumber;
    }

    private final Long id;
    private final String permitNumber;
    private final String permitType;
    private final String permitTypeCode;
    private final Long originalPermitId;
    private final HarvestReportState harvestReportState;
    private final Set<Integer> gameSpeciesCodes;
    private final List<HarvestPermitSpeciesAmountWithAmendmentDTO> speciesAmounts;
    private final Set<String> amendmentPermitNumbers;
    private final boolean canAddHarvest;
    private final boolean canEndHunting;
    private final boolean canDownloadDecision;
    private final GrantStatus grantStatus;
    private final PermitDecision.DecisionType decisionType;
    private final String decisionDocumentNumber;

    public Long getId() {
        return id;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public Set<String> getAmendmentPermitNumbers() {
        return amendmentPermitNumbers;
    }

    public String getPermitType() {
        return permitType;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public Long getOriginalPermitId() {
        return originalPermitId;
    }

    public Set<Integer> getGameSpeciesCodes() {
        return gameSpeciesCodes;
    }

    public List<HarvestPermitSpeciesAmountWithAmendmentDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public boolean isCanAddHarvest() {
        return canAddHarvest;
    }

    public boolean isCanEndHunting() {
        return canEndHunting;
    }

    public boolean isCanDownloadDecision() {
        return canDownloadDecision;
    }

    public GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public PermitDecision.DecisionType getDecisionType() {
        return decisionType;
    }

    public String getDecisionDocumentNumber() {
        return decisionDocumentNumber;
    }
}
