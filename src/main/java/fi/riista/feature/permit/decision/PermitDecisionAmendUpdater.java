package fi.riista.feature.permit.decision;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermitDecisionAmendUpdater {

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Transactional
    public void updateDecision(final PermitDecision decision) {
        final HarvestPermitApplication application = decision.getApplication();
        decision.setPermitHolder(application.getPermitHolder());
        decision.setContactPerson(application.getContactPerson());
        decision.setRhy(application.getRhy());
        decision.setHta(application.getArea().findLargestHta().orElse(null));

        final PermitDecisionDocument document = decision.getDocument();
        document.setApplication(permitDecisionTextService.generateApplicationSummary(decision));

        final List<PermitDecisionSpeciesAmount> toBeAdded = getSpeciesMissingFromDecision(decision, application);
        final List<PermitDecisionSpeciesAmount> toBeRemoved = updateCurrentSpecies(decision, application);

        permitDecisionSpeciesAmountRepository.save(toBeAdded);
        permitDecisionSpeciesAmountRepository.delete(toBeRemoved);

        decision.getSpeciesAmounts().addAll(toBeAdded);
        decision.getSpeciesAmounts().removeAll(toBeRemoved);

        permitDecisionTextService.fillInBlanks(decision);

        decision.updateGrantStatus();
    }

    private static List<PermitDecisionSpeciesAmount> updateCurrentSpecies(final PermitDecision decision,
                                                                          final HarvestPermitApplication application) {
        final Map<Long, Float> applicationAmountMapping = application.getSpeciesAmounts().stream()
                .collect(Collectors.toMap(spa -> spa.getGameSpecies().getId(),
                        HarvestPermitApplicationSpeciesAmount::getAmount));

        final List<PermitDecisionSpeciesAmount> toBeRemoved = new LinkedList<>();

        for (final PermitDecisionSpeciesAmount decisionSpeciesAmount : decision.getSpeciesAmounts()) {
            final Float applicationAmount = applicationAmountMapping.get(decisionSpeciesAmount.getGameSpecies().getId());

            if (applicationAmount == null) {
                // Remove species not included in application
                toBeRemoved.add(decisionSpeciesAmount);
            } else if (decisionSpeciesAmount.getAmount() > applicationAmount) {
                // Limit decision species amount to application amount
                decisionSpeciesAmount.setAmount(applicationAmount);
            }
        }

        return toBeRemoved;
    }

    private static List<PermitDecisionSpeciesAmount> getSpeciesMissingFromDecision(final PermitDecision decision,
                                                                                   final HarvestPermitApplication application) {
        final Set<GameSpecies> currentDecisionSpecies = decision.getSpeciesAmounts().stream()
                .map(PermitDecisionSpeciesAmount::getGameSpecies)
                .collect(Collectors.toSet());

        final List<PermitDecisionSpeciesAmount> toBeAdded = new LinkedList<>();

        for (HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount : application.getSpeciesAmounts()) {
            if (!currentDecisionSpecies.contains(applicationSpeciesAmount.getGameSpecies())) {
                toBeAdded.add(createDecisionSpecies(applicationSpeciesAmount, decision));
            }
        }

        return toBeAdded;
    }

    private static PermitDecisionSpeciesAmount createDecisionSpecies(final HarvestPermitApplicationSpeciesAmount source,
                                                                     final PermitDecision decision) {
        final PermitDecisionSpeciesAmount target = new PermitDecisionSpeciesAmount();
        target.setPermitDecision(decision);
        target.setGameSpecies(source.getGameSpecies());
        target.setAmount(source.getAmount());

        final int huntingYear = decision.getApplication().getHuntingYear();
        target.setBeginDate(HarvestPermit.getDefaultMooselikeBeginDate(huntingYear));
        target.setEndDate(HarvestPermit.getDefaultMooselikeEndDate(huntingYear));

        return target;
    }
}
