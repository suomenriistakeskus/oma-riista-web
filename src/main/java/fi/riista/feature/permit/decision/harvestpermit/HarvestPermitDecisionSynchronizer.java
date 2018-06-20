package fi.riista.feature.permit.decision.harvestpermit;

import com.google.common.base.Preconditions;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class HarvestPermitDecisionSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitDecisionSynchronizer.class);

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitModificationRestriction harvestPermitModificationRestriction;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void synchronize(final PermitDecision permitDecision) {
        final List<HarvestPermit> existingPermits = harvestPermitRepository.findByPermitDecision(permitDecision);

        if (existingPermits.isEmpty()) {
            createHarvestPermit(permitDecision);
            return;
        }

        if (existingPermits.size() > 1) {
            LOG.error(String.format("Invalid HarvestPermit count %d for decisionId %d",
                    existingPermits.size(), permitDecision.getId()));
            return;
        }

        final HarvestPermit harvestPermit = existingPermits.get(0);

        if (harvestPermitModificationRestriction.canModifyHarvestPermit(permitDecision, harvestPermit)) {
            final int permitAreaSize = getPermitAreaSize(permitDecision);

            harvestPermit.setPermitAreaSize(permitAreaSize);
            harvestPermit.setPermitHolder(permitDecision.getPermitHolder());
            harvestPermit.setOriginalContactPerson(permitDecision.getContactPerson());
            harvestPermit.setRhy(permitDecision.getRhy());
            harvestPermit.setMooseArea(permitDecision.getHta());
            harvestPermit.getRelatedRhys().clear();
            harvestPermit.getRelatedRhys().addAll(permitDecision.getApplication().getRelatedRhys());
            harvestPermit.getPermitPartners().clear();
            harvestPermit.getPermitPartners().addAll(permitDecision.getApplication().getPermitPartners());
            updateHarvestPermitSpecies(permitDecision, harvestPermit);

        } else {
            LOG.warn(String.format("Refusing to update HarvestPermit id=%d permitNumber=%s with existing user data",
                    harvestPermit.getId(), harvestPermit.getPermitNumber()));
        }
    }

    private void createHarvestPermit(final PermitDecision permitDecision) {
        final String permitTypeCode = permitDecision.getApplication().getPermitTypeCode();
        Preconditions.checkArgument(HarvestPermit.isMooselikePermitTypeCode(permitTypeCode),
                "Only mooselike is supported");

        final int permitAreaSize = getPermitAreaSize(permitDecision);

        final HarvestPermit harvestPermit = new HarvestPermit();
        harvestPermit.setPermitDecision(permitDecision);
        harvestPermit.setPermitHolder(permitDecision.getPermitHolder());
        harvestPermit.setOriginalContactPerson(permitDecision.getContactPerson());
        harvestPermit.setRhy(permitDecision.getRhy());
        harvestPermit.setMooseArea(permitDecision.getHta());

        harvestPermit.setPermitNumber(permitDecision.getApplication().getPermitNumber());
        harvestPermit.setPermitType(HarvestPermit.MOOSELIKE_PERMIT_NAME.getTranslation(permitDecision.getLocale()));
        harvestPermit.setPermitTypeCode(permitTypeCode);
        harvestPermit.setPermitAreaSize(permitAreaSize);

        harvestPermit.getRelatedRhys().addAll(permitDecision.getApplication().getRelatedRhys());
        harvestPermit.getPermitPartners().addAll(permitDecision.getApplication().getPermitPartners());

        final List<HarvestPermitSpeciesAmount> harvestPermitSpeciesAmounts = createSpeciesAmounts(
                permitDecision.getSpeciesAmounts(), harvestPermit);

        harvestPermitRepository.save(harvestPermit);
        harvestPermitSpeciesAmountRepository.save(harvestPermitSpeciesAmounts);
    }

    private int getPermitAreaSize(final PermitDecision permitDecision) {
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(
                permitDecision.getApplication().getArea().getZone().getId());
        final Long permitAreaSize = NumberUtils.squareMetersToHectares(areaSize.getAll().getTotal());
        return permitAreaSize.intValue();
    }

    private void updateHarvestPermitSpecies(final PermitDecision permitDecision,
                                            final HarvestPermit harvestPermit) {
        final List<PermitDecisionSpeciesAmount> missingSpeciesAmounts = new LinkedList<>();
        final List<HarvestPermitSpeciesAmount> redundantSpeciesAmounts = new LinkedList<>();

        for (final PermitDecisionSpeciesAmount a : permitDecision.getSpeciesAmounts()) {
            final Optional<HarvestPermitSpeciesAmount> existing = harvestPermit.getSpeciesAmounts().stream()
                    .filter(b -> Objects.equals(a.getGameSpecies(), b.getGameSpecies()))
                    .findAny();

            if (existing.isPresent()) {
                if (a.getAmount() > 0) {
                    updateSpeciesAmount(a, existing.get());
                } else {
                    redundantSpeciesAmounts.add(existing.get());
                }
            } else {
                missingSpeciesAmounts.add(a);
            }
        }

        for (final HarvestPermitSpeciesAmount a : harvestPermit.getSpeciesAmounts()) {
            if (permitDecision.getSpeciesAmounts().stream()
                    .filter(spa -> spa.getAmount() > 0)
                    .noneMatch(b -> Objects.equals(a.getGameSpecies(), b.getGameSpecies()))) {
                redundantSpeciesAmounts.add(a);
            }
        }

        final List<HarvestPermitSpeciesAmount> newSpeciesAmounts =
                createSpeciesAmounts(missingSpeciesAmounts, harvestPermit);
        harvestPermit.getSpeciesAmounts().removeAll(redundantSpeciesAmounts);
        harvestPermit.getSpeciesAmounts().addAll(newSpeciesAmounts);

        harvestPermitSpeciesAmountRepository.save(newSpeciesAmounts);
    }

    @Nonnull
    private static List<HarvestPermitSpeciesAmount> createSpeciesAmounts(
            final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts, final HarvestPermit harvestPermit) {
        return decisionSpeciesAmounts.stream()
                .filter(spa -> spa.getAmount() > 0)
                .map(spa -> createSpeciesAmount(harvestPermit, spa))
                .collect(toList());
    }

    @Nonnull
    private static HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit harvestPermit, final PermitDecisionSpeciesAmount decisionSpeciesAmount) {
        final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount = new HarvestPermitSpeciesAmount(harvestPermit,
                decisionSpeciesAmount.getGameSpecies(),
                decisionSpeciesAmount.getAmount(),
                getRestrictionType(decisionSpeciesAmount),
                decisionSpeciesAmount.getRestrictionAmount(),
                decisionSpeciesAmount.getBeginDate(),
                decisionSpeciesAmount.getEndDate(),
                null);
        harvestPermitSpeciesAmount.setBeginDate2(decisionSpeciesAmount.getBeginDate2());
        harvestPermitSpeciesAmount.setEndDate2(decisionSpeciesAmount.getEndDate2());
        return harvestPermitSpeciesAmount;
    }

    private static void updateSpeciesAmount(final PermitDecisionSpeciesAmount permitDecisionSpeciesAmount,
                                            final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount) {
        harvestPermitSpeciesAmount.setBeginDate(permitDecisionSpeciesAmount.getBeginDate());
        harvestPermitSpeciesAmount.setEndDate(permitDecisionSpeciesAmount.getEndDate());
        harvestPermitSpeciesAmount.setBeginDate2(permitDecisionSpeciesAmount.getBeginDate2());
        harvestPermitSpeciesAmount.setEndDate2(permitDecisionSpeciesAmount.getEndDate2());
        harvestPermitSpeciesAmount.setAmount(permitDecisionSpeciesAmount.getAmount());
        harvestPermitSpeciesAmount.setRestrictionAmount(permitDecisionSpeciesAmount.getRestrictionAmount());
        harvestPermitSpeciesAmount.setRestrictionType(getRestrictionType(permitDecisionSpeciesAmount));
    }

    private static HarvestPermitSpeciesAmount.RestrictionType getRestrictionType(
            final PermitDecisionSpeciesAmount decisionSpeciesAmount) {

        if (decisionSpeciesAmount.getRestrictionType() == null) {
            return null;
        }

        switch (decisionSpeciesAmount.getRestrictionType()) {
            case AE:
                return HarvestPermitSpeciesAmount.RestrictionType.AE;
            case AU:
                return HarvestPermitSpeciesAmount.RestrictionType.AU;

            default:
                throw new IllegalArgumentException("Unknown restriction type: "
                        + decisionSpeciesAmount.getRestrictionType());
        }
    }
}
