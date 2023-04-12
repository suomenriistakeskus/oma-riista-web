package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.permit.PermitAlertLogging;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.util.F;
import org.joda.time.LocalDate;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static fi.riista.util.F.mapNullable;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    @Resource
    private ActiveUserService activeUserService;

    private Map<Integer, List<PermitDecisionSpeciesAmount>> groupDecisionSpeciesByYear(
            final @Nonnull PermitDecision permitDecision) {
        // Do not filter by hasGrantedSpecies in this stage to enable revoking certain species
        final Map<Integer, List<PermitDecisionSpeciesAmount>> map = permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision).stream()
                .collect(groupingBy(PermitDecisionSpeciesAmount::getPermitYear, toList()));

        map.entrySet().forEach(e-> {
            final int distinctSpeciesCount = e.getValue().stream()
                    .map(PermitDecisionSpeciesAmount::getGameSpecies)
                    .map(GameSpecies::getId)
                    .collect(toSet()).size();
            if (distinctSpeciesCount != e.getValue().size()) {
                LOG.warn("{} Duplicate species for permit year {}", PermitAlertLogging.PERMIT_ALERT_PREFIX, e.getKey());
            }
        });

        return map;
    }

    private static boolean hasGrantedSpecies(final List<PermitDecisionSpeciesAmount> speciesAmounts) {
        return speciesAmounts.stream().anyMatch(PermitDecisionSpeciesAmount::hasGrantedSpecies);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void synchronize(final PermitDecision permitDecision) {
        final List<HarvestPermit> existingPermits = harvestPermitRepository.findByPermitDecision(permitDecision);

        switch (permitDecision.getDecisionType()) {
            case HARVEST_PERMIT:
                doSynchronize(permitDecision, existingPermits);
                break;
            case CANCEL_ANNUAL_RENEWAL:
                // Skip permit synchronization
                break;
            default:
                removePermits(existingPermits);
                break;
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HarvestPermitDTO createNewAnnualPermit(final PermitDecision permitDecision, final int permitYear) {
        checkArgument(permitDecision.isAnnualUnprotectedBird(), "Only annual permits supported");
        checkArgument(permitDecision.getDecisionType() == PermitDecision.DecisionType.HARVEST_PERMIT,
                "Only harvest permit decisions supported");

        final List<PermitDecisionSpeciesAmount> decisionAmounts =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision);

        checkArgument(hasGrantedSpecies(decisionAmounts), "Decision has no granted species");

        final HarvestPermit latestPermit =
                harvestPermitRepository.findByPermitNumber(permitDecision.createPermitNumber(permitYear - 1));

        checkArgument(latestPermit != null, "Invalid permit year, no permit exists for previous year");
        checkArgument(latestPermit.isHarvestReportApproved(), "Only finished permits can be renewed");

        final String permitNumber = permitDecision.createPermitNumber(permitYear);
        final HarvestPermit harvestPermit = doCreatePermit(permitDecision, permitNumber);

        final List<HarvestPermitSpeciesAmount> amounts = latestPermit.getSpeciesAmounts().stream()
                .map(spa -> {
                    final HarvestPermitSpeciesAmount newAmount = new HarvestPermitSpeciesAmount();
                    newAmount.setHarvestPermit(harvestPermit);

                    newAmount.setGameSpecies(spa.getGameSpecies());
                    newAmount.setSpecimenAmount(spa.getSpecimenAmount());
                    newAmount.setBeginDate(spa.getBeginDate().plusYears(1));
                    newAmount.setEndDate(spa.getEndDate().plusYears(1));
                    newAmount.setBeginDate2(mapNullable(spa.getBeginDate2(), bd2 -> bd2.plusYears(1)));
                    newAmount.setEndDate2(mapNullable(spa.getEndDate2(), ed2 -> ed2.plusYears(1)));
                    return newAmount;
                })
                .collect(toList());

        harvestPermitRepository.save(harvestPermit);
        harvestPermitSpeciesAmountRepository.saveAll(amounts);

        return HarvestPermitDTO.create(harvestPermit, amounts, emptySet(), emptyList(),
                activeUserService.requireActiveUser(), permitDecision.getGrantStatus(), permitDecision);
    }

    private void doSynchronize(final PermitDecision permitDecision, final List<HarvestPermit> existingPermits) {
        if (!existingPermits.isEmpty()) {
            updatePermits(permitDecision, existingPermits);
        } else {
            createPermits(permitDecision);
        }

        if (harvestPermitRepository.findByPermitDecision(permitDecision).isEmpty()) {
            // If all existing permits were removed, create empty permit to allow applicant to pay the
            // handling fee through the permit management view
            harvestPermitRepository.flush();
            createEmptyPermit(permitDecision);
        }
    }

    private void createPermits(final PermitDecision permitDecision) {
        // Create empty permit for first permit year for rejected decisions
        // to allow user to access permit management view for processing invoice payment
        if (permitDecision.getGrantStatus() == GrantStatus.REJECTED) {
            createEmptyPermit(permitDecision);
        } else if (permitDecision.getApplication().getHarvestPermitCategory().hasSpeciesAmount()) {
            groupDecisionSpeciesByYear(permitDecision).forEach((year, amountList) -> {
                final String permitNumber = permitDecision.createPermitNumber(year);
                final HarvestPermitCreateResult result = createPermit(permitDecision, amountList, permitNumber);

                harvestPermitRepository.saveAll(result.getPermits());
                harvestPermitSpeciesAmountRepository.saveAll(result.getSpeciesAmounts());
            });
        } else {
            final String permitNumber = permitDecision.createPermitNumber();
            final HarvestPermit permit = doCreatePermit(permitDecision, permitNumber);
            harvestPermitRepository.save(permit);
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
        if (permitDecision.getApplication().getHarvestPermitCategory().hasSpeciesAmount()) {
            updateHarvestPermits(permitDecision, existingPermits);
        } else {
            updateOtherPermits(permitDecision, existingPermits);
        }
    }

    private void updatePermit(final PermitDecision permitDecision,
                              final HarvestPermit harvestPermit) {
        harvestPermitUpdateService.updateHarvestPermit(permitDecision, harvestPermit);
    }

    private void updatePermit(final PermitDecision permitDecision,
                              final HarvestPermit harvestPermit,
                              final List<PermitDecisionSpeciesAmount> sourceAmounts) {
        final List<HarvestPermitSpeciesAmount> targetAmounts =
                harvestPermitSpeciesAmountRepository.findByHarvestPermit(harvestPermit);

        updatePermit(permitDecision, harvestPermit);

        final HarvestPermitSpeciesUpdateResult updateResult = harvestPermitUpdateService
                .updateHarvestPermitSpecies(permitDecision, harvestPermit, sourceAmounts, targetAmounts);

        if (updateResult.hasCreated()) {
            harvestPermitSpeciesAmountRepository.saveAll(updateResult.getCreated());
        }

        if (updateResult.hasDeleted()) {
            harvestPermitSpeciesAmountRepository.deleteAll(updateResult.getDeleted());
        }
    }

    private void updateOtherPermits(final PermitDecision permitDecision, final List<HarvestPermit> existingPermits) {
        checkState(existingPermits.size() == 1);
        updatePermit(permitDecision, existingPermits.get(0));
    }

    private void updateHarvestPermits(final PermitDecision permitDecision, final List<HarvestPermit> existingPermits) {
        final Map<String, HarvestPermit> existingPermitIndex = F.index(existingPermits, HarvestPermit::getPermitNumber);
        final List<HarvestPermit> redundantPermits = new LinkedList<>(existingPermits);

        groupDecisionSpeciesByYear(permitDecision).forEach((year, amountList) -> {
            // Update permits only when species have been granted
            // Permits without granted species will be removed through redundant permits
            if (hasGrantedSpecies(amountList)) {
                // TODO: How to match permits if decisionYear or validityYear is updated
                final String permitNumber = permitDecision.createPermitNumber(year);
                final HarvestPermit existingPermit = existingPermitIndex.get(permitNumber);

                if (existingPermit == null) {
                    final HarvestPermitCreateResult result = createPermit(permitDecision, amountList, permitNumber);
                    harvestPermitRepository.saveAll(result.getPermits());
                    harvestPermitSpeciesAmountRepository.saveAll(result.getSpeciesAmounts());

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
            }
        });

        // Follow-up decision for annual unprotected bird are made by updating species date interval
        // and therefore existing permits should not be modified or removed.
        if (!permitDecision.isAnnualUnprotectedBird()) {
            // Remove other permits linked to decision if possible
            removePermits(redundantPermits);
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

    private void createEmptyPermit(final PermitDecision permitDecision) {
        final String permitNumber = permitDecision.createPermitNumber(permitDecision.getDecisionYear());
        harvestPermitRepository.save(doCreatePermit(permitDecision, permitNumber));
    }
}
