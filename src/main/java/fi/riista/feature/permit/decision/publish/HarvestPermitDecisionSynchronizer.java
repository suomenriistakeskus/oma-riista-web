package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class HarvestPermitDecisionSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitDecisionSynchronizer.class);

    @Resource
    private HarvestPermitModificationRestriction harvestPermitModificationRestriction;

    @Resource
    private HarvestPermitUpdateService harvestPermitUpdateService;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    private Map<Integer, List<PermitDecisionSpeciesAmount>> groupDecisionSpeciesByYear(
            final @Nonnull PermitDecision permitDecision) {
        return permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision).stream()
                .collect(groupingBy(PermitDecisionSpeciesAmount::getPermitYear, toList()));
    }

    private static boolean hasGrantedSpecies(final List<PermitDecisionSpeciesAmount> speciesAmounts) {
        return speciesAmounts.stream().anyMatch(PermitDecisionSpeciesAmount::hasGrantedSpecies);
    }

    private static boolean shouldCreateHarvestPermit(final PermitDecision permitDecision) {
        return permitDecision.getDecisionType() == PermitDecision.DecisionType.HARVEST_PERMIT;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void synchronize(final PermitDecision permitDecision) {
        final List<HarvestPermit> existingPermits = harvestPermitRepository.findByPermitDecision(permitDecision);

        if (shouldCreateHarvestPermit(permitDecision)) {
            if (!existingPermits.isEmpty()) {
                updatePermits(permitDecision, existingPermits);
            } else {
                createPermits(permitDecision);
            }

        } else if (!existingPermits.isEmpty()) {
            removePermits(existingPermits);
        }
    }

    private void createPermits(final PermitDecision permitDecision) {
        // Create empty permit for first permit year for rejected decisions
        // to allow user to access permit management view for processing invoice payment
        if (permitDecision.getGrantStatus() == PermitDecision.GrantStatus.REJECTED) {
            final String permitNumber = permitDecision.createPermitNumber(permitDecision.getDecisionYear());
            harvestPermitRepository.save(doCreatePermit(permitDecision, permitNumber));
        } else {
            groupDecisionSpeciesByYear(permitDecision).forEach((year, amountList) -> {
                final String permitNumber = permitDecision.createPermitNumber(year);
                final HarvestPermitCreateResult result = createPermit(permitDecision, amountList, permitNumber);

                harvestPermitRepository.save(result.getPermits());
                harvestPermitSpeciesAmountRepository.save(result.getSpeciesAmounts());
            });
        }
    }

    private HarvestPermitCreateResult createPermit(final PermitDecision permitDecision,
                                                   final List<PermitDecisionSpeciesAmount> sourceAmounts,
                                                   final String permitNumber) {
        if (!hasGrantedSpecies(sourceAmounts)) {
            // Skip creation if no species has granted amounts
            return new HarvestPermitCreateResult();
        }

        final HarvestPermit harvestPermit = doCreatePermit(permitDecision, permitNumber);

        final List<HarvestPermitSpeciesAmount> speciesAmounts = sourceAmounts.stream()
                .filter(PermitDecisionSpeciesAmount::hasGrantedSpecies)
                .map(spa -> HarvestPermitSpeciesAmountOps.create(harvestPermit, spa))
                .collect(toList());

        return new HarvestPermitCreateResult(harvestPermit, speciesAmounts);
    }

    private HarvestPermit doCreatePermit(final PermitDecision permitDecision, final String permitNumber) {
        final HarvestPermit harvestPermit = HarvestPermit.create(permitNumber);
        harvestPermit.setPermitDecision(permitDecision);

        harvestPermitUpdateService.updateHarvestPermit(permitDecision, harvestPermit);
        return harvestPermit;
    }

    private void updatePermits(final PermitDecision permitDecision, final List<HarvestPermit> existingPermits) {
        final Map<String, HarvestPermit> existingPermitIndex = F.index(existingPermits, HarvestPermit::getPermitNumber);
        final List<HarvestPermit> redundantPermits = new LinkedList<>(existingPermits);

        groupDecisionSpeciesByYear(permitDecision).forEach((year, amountList) -> {
            // TODO: How to match permits if decisionYear or validityYear is updated
            final String permitNumber = permitDecision.createPermitNumber(year);
            final HarvestPermit existingPermit = existingPermitIndex.get(permitNumber);

            if (existingPermit == null) {
                final HarvestPermitCreateResult result = createPermit(permitDecision, amountList, permitNumber);
                harvestPermitRepository.save(result.getPermits());
                harvestPermitSpeciesAmountRepository.save(result.getSpeciesAmounts());

            } else {
                redundantPermits.remove(existingPermit);

                if (harvestPermitModificationRestriction.canModifyHarvestPermit(existingPermit)) {
                    updatePermit(permitDecision, existingPermit, amountList);
                } else {
                    LOG.warn(String.format(
                            "Refusing to update HarvestPermit id=%d permitNumber=%s with existing user data",
                            existingPermit.getId(), existingPermit.getPermitNumber()));
                }
            }
        });

        // Follow-up decision for annual unprotected bird are made by updating species date interval
        // and therefore existing permits should not be modified or removed.
        if (!permitDecision.isAnnualUnprotectedBird()) {
            // Remove other permits linked to decision if possible
            removePermits(redundantPermits);
        }
    }

    private void updatePermit(final PermitDecision permitDecision,
                              final HarvestPermit harvestPermit,
                              final List<PermitDecisionSpeciesAmount> sourceAmounts) {
        final List<HarvestPermitSpeciesAmount> targetAmounts =
                harvestPermitSpeciesAmountRepository.findByHarvestPermit(harvestPermit);

        harvestPermitUpdateService.updateHarvestPermit(permitDecision, harvestPermit);

        final HarvestPermitSpeciesUpdateResult updateResult = harvestPermitUpdateService
                .updateHarvestPermitSpecies(permitDecision, harvestPermit, sourceAmounts, targetAmounts);

        if (updateResult.hasCreated()) {
            harvestPermitSpeciesAmountRepository.save(updateResult.getCreated());
        }

        if (updateResult.hasDeleted()) {
            harvestPermitSpeciesAmountRepository.delete(updateResult.getDeleted());
        }
    }

    private void removePermits(final List<HarvestPermit> existingPermits) {
        for (HarvestPermit permit : existingPermits) {
            if (harvestPermitModificationRestriction.canModifyHarvestPermit(permit)) {
                harvestPermitSpeciesAmountRepository.deleteByHarvestPermit(permit);
                harvestPermitRepository.delete(permit);
            } else {
                LOG.warn(String.format("Refusing to delete HarvestPermit id=%d permitNumber=%s with existing user data",
                        permit.getId(), permit.getPermitNumber()));
            }
        }
    }
}
