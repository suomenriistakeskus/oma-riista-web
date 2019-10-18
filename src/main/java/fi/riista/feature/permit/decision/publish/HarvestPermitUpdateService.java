package fi.riista.feature.permit.decision.publish;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public class HarvestPermitUpdateService {

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHarvestPermit(final @Nonnull PermitDecision permitDecision,
                                    final @Nonnull HarvestPermit harvestPermit) {
        requireNonNull(permitDecision);
        requireNonNull(harvestPermit);

        final HarvestPermitApplication application = permitDecision.getApplication();

        harvestPermit.setPermitTypeCode(permitDecision.getPermitTypeCode());
        harvestPermit.setPermitType(permitDecision.getDecisionName());
        harvestPermit.setPermitHolder(permitDecision.getPermitHolder());
        harvestPermit.setHuntingClub(permitDecision.getHuntingClub());
        harvestPermit.setOriginalContactPerson(permitDecision.getContactPerson());
        harvestPermit.setRhy(permitDecision.getRhy());
        harvestPermit.setMooseArea(permitDecision.getHta());
        harvestPermit.setOriginalPermit(getOriginalPermit(application));
        harvestPermit.setPermitAreaSize(getPermitAreaSize(application));
        harvestPermit.setHarvestsAsList(PermitTypeCode.checkIsHarvestsAsList(permitDecision.getPermitTypeCode()));

        harvestPermit.getRelatedRhys().clear();
        harvestPermit.getPermitPartners().clear();

        harvestPermit.getRelatedRhys().addAll(application.getRelatedRhys());
        harvestPermit.getPermitPartners().addAll(application.getPermitPartners());
    }

    private HarvestPermit getOriginalPermit(final HarvestPermitApplication application) {
        return application.getHarvestPermitCategory().isAmendment()
                ? amendmentApplicationDataRepository.getByApplication(application).getOriginalPermit()
                : null;
    }

    private Integer getPermitAreaSize(final HarvestPermitApplication application) {
        if (application.getHarvestPermitCategory().isMooselike()) {
            return Optional.ofNullable(application.getArea())
                    .map(HarvestPermitArea::getZone)
                    .map(F::getId)
                    .map(zoneId -> gisZoneRepository.getAreaSize(zoneId))
                    .map(areaSize -> NumberUtils.squareMetersToHectares(areaSize.getAll().getLand()))
                    .map(Long::intValue)
                    .orElse(null);
        }

        return null;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HarvestPermitSpeciesUpdateResult updateHarvestPermitSpecies(
            final @Nonnull PermitDecision permitDecision,
            final @Nonnull HarvestPermit harvestPermit,
            final @Nonnull List<PermitDecisionSpeciesAmount> sourceAmounts,
            final @Nonnull List<HarvestPermitSpeciesAmount> targetAmounts) {

        requireNonNull(permitDecision);
        requireNonNull(harvestPermit);
        requireNonNull(sourceAmounts);
        requireNonNull(targetAmounts);

        Preconditions.checkArgument(permitDecision.getDecisionType() == PermitDecision.DecisionType.HARVEST_PERMIT);

        final HarvestPermitSpeciesUpdateResult result = new HarvestPermitSpeciesUpdateResult();

        final Map<GameSpecies, PermitDecisionSpeciesAmount> sourceIndex =
                F.index(sourceAmounts, PermitDecisionSpeciesAmount::getGameSpecies);

        final Map<GameSpecies, HarvestPermitSpeciesAmount> targetIndex =
                F.index(targetAmounts, HarvestPermitSpeciesAmount::getGameSpecies);

        for (final PermitDecisionSpeciesAmount source : sourceAmounts) {
            if (hasGrantedSpecies(source)) {
                final HarvestPermitSpeciesAmount target = targetIndex.get(source.getGameSpecies());

                if (target != null) {
                    // update
                    HarvestPermitSpeciesAmountOps.copy(source, target);
                    result.addUpdated(target);

                } else {
                    // create
                    result.addCreated(HarvestPermitSpeciesAmountOps.create(harvestPermit, source));
                }
            }
        }

        for (final HarvestPermitSpeciesAmount target : targetAmounts) {
            final PermitDecisionSpeciesAmount source = sourceIndex.get(target.getGameSpecies());

            if (source == null || !(hasGrantedSpecies(source))) {
                // delete
                result.addDeleted(target);
            }
        }

        return result;
    }

    private static boolean hasGrantedSpecies(final PermitDecisionSpeciesAmount speciesAmount) {
        return speciesAmount.getAmount() > 0;
    }
}
