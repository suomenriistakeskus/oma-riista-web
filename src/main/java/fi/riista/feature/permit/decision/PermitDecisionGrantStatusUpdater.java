package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class PermitDecisionGrantStatusUpdater {
    private final List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts;
    private final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts;

    public PermitDecisionGrantStatusUpdater(final List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts,
                                            final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts) {
        this.applicationSpeciesAmounts = requireNonNull(applicationSpeciesAmounts);
        this.decisionSpeciesAmounts = requireNonNull(decisionSpeciesAmounts);
    }

    public PermitDecision.GrantStatus calculate() {
        final double decisionAmountSum = decisionAmountSum().sum();

        if (decisionAmountSum < 0.5) {
            return PermitDecision.GrantStatus.REJECTED;
        } else if (decisionHasRestrictions() || !decisionAmountMatchApplication()) {
            return PermitDecision.GrantStatus.RESTRICTED;
        } else {
            return PermitDecision.GrantStatus.UNCHANGED;
        }
    }

    @Nonnull
    private DoubleStream decisionAmountSum() {
        return decisionSpeciesAmounts.stream().mapToDouble(PermitDecisionSpeciesAmount::getAmount);
    }

    private boolean decisionHasRestrictions() {
        return decisionSpeciesAmounts.stream().anyMatch(a -> a.getRestrictionType() != null);
    }

    private boolean decisionAmountMatchApplication() {
        return Objects.equals(expectedApplicationTotalAmount(), grantedTotalDecisionAmount());
    }

    private Map<Integer, List<Float>> grantedTotalDecisionAmount() {
        return decisionSpeciesAmounts.stream()
                // Group by species
                .collect(groupingBy(a -> a.getGameSpecies().getOfficialCode(),
                        mapping(PermitDecisionSpeciesAmount::getAmount, toList())));
    }

    private Map<Integer, List<Float>> expectedApplicationTotalAmount() {
        return applicationSpeciesAmounts.stream()
                .flatMap(source -> IntStream.range(0, validityYears(source)).mapToObj(a -> source))
                // Group by species
                .collect(groupingBy(a -> a.getGameSpecies().getOfficialCode(),
                        mapping(HarvestPermitApplicationSpeciesAmount::getAmount, toList())));
    }

    private static int validityYears(final HarvestPermitApplicationSpeciesAmount source) {
        return Optional.ofNullable(source.getValidityYears()).filter(a -> a > 0).orElse(1);
    }
}
