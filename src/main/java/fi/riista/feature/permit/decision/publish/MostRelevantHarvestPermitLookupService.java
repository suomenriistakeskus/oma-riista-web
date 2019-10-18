package fi.riista.feature.permit.decision.publish;

import com.google.common.collect.Sets;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.util.Collect;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

@Service
public class MostRelevantHarvestPermitLookupService {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public HarvestPermit lookupMostRelevant(final @Nonnull PermitDecision permitDecision) {
        requireNonNull(permitDecision);

        final Map<String, HarvestPermit> permitIndex = harvestPermitRepository
                .findByPermitDecision(permitDecision).stream()
                .collect(Collect.indexingBy(HarvestPermit::getPermitNumber));

        final Set<String> decisionPermitNumbers = getDecisionPermitNumbers(permitDecision);
        final Set<String> harvestPermitNumbers = permitIndex.keySet();

        return permitIndex.get(mostRelevantPermitNumber(decisionPermitNumbers, harvestPermitNumbers));
    }

    private Set<String> getDecisionPermitNumbers(@Nonnull final PermitDecision permitDecision) {
        // Generate all currently granted permit numbers
        return permitDecisionSpeciesAmountRepository
                .findByPermitDecision(permitDecision).stream()
                .filter(PermitDecisionSpeciesAmount::hasGrantedSpecies)
                .map(spa -> permitDecision.createPermitNumber(spa.getPermitYear()))
                .collect(toSet());
    }

    static String mostRelevantPermitNumber(final @Nonnull Set<String> decisionPermitNumbers,
                                           final @Nonnull Set<String> harvestPermitNumbers) {
        if (harvestPermitNumbers.isEmpty()) {
            return null;
        }

        // Compare to existing permit numbers
        final Set<String> possiblePermitNumbers = Sets.intersection(harvestPermitNumbers, decisionPermitNumbers);

        // Fallback to existing permit numbers if none match
        return possiblePermitNumbers.isEmpty()
                ? Collections.min(harvestPermitNumbers)
                : Collections.min(possiblePermitNumbers);
    }
}
